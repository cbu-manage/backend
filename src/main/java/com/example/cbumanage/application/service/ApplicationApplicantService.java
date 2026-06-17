package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicantApplicationResponse;
import com.example.cbumanage.application.dto.ApplicationCancelRequest;
import com.example.cbumanage.application.dto.ApplicationDetailResponse;
import com.example.cbumanage.application.dto.ApplicationMyRequest;
import com.example.cbumanage.application.dto.ApplicationSubmitRequest;
import com.example.cbumanage.application.entity.ApplicationAnswer;
import com.example.cbumanage.application.entity.ApplicationPortfolioUrl;
import com.example.cbumanage.application.entity.ApplicationQuestion;
import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import com.example.cbumanage.application.repository.ApplicationAnswerRepository;
import com.example.cbumanage.application.repository.ApplicationPortfolioUrlRepository;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import com.example.cbumanage.email.service.EmailManager;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.global.util.RedisUtil;
import com.example.cbumanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationApplicantService {

    private final RecruitmentRepository recruitmentRepository;
    private final MemberApplicationRepository memberApplicationRepository;
    private final ApplicationAnswerRepository applicationAnswerRepository;
    private final ApplicationPortfolioUrlRepository applicationPortfolioUrlRepository;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;
    private final ApplicationQuestionService applicationQuestionService;
    private final EmailManager emailManager;
    private final RecruitmentGenerationPolicy generationPolicy;

    @Transactional
    public ApplicantApplicationResponse submit(ApplicationSubmitRequest request) {
        validateTukoreaEmailAuth(request.email(), request.emailAuthCode(), false);

        userRepository.findByStudentNumber(request.studentNumber()).ifPresent(user -> {
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        });

        Long generation = currentApplicationGeneration();
        memberApplicationRepository.findByStudentNumberAndGeneration(
                request.studentNumber(), generation).ifPresent(application -> {
            throw new BaseException(ErrorCode.APPLICATION_DUPLICATED);
        });

        MemberApplication application = memberApplicationRepository.save(MemberApplication.builder()
                .studentNumber(request.studentNumber())
                .email(request.email())
                .name(request.name())
                .nickname(request.nickname())
                .grade(request.grade())
                .major(request.major())
                .phoneNumber(request.phoneNumber())
                .generation(generation)
                .applicationField(request.applicationField())
                .portfolioUrl(resolvePrimaryPortfolioUrl(request))
                .refSource(request.refSource())
                .refLinkEtc(request.refLinkEtc())
                .canOt(request.canOt())
                .canWelcome(request.canWelcome())
                .privacyPolicy(request.privacyPolicy())
                .build());

        saveAnswers(application, request.answers());
        savePortfolios(application, request.portfolios());
        redisUtil.deleteData(request.email());

        return toApplicantResponse(application);
    }

    private Long currentApplicationGeneration() {
        return recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)
                .map(recruitment -> recruitment.getGeneration())
                .orElseGet(generationPolicy::currentGeneration);
    }

    @Transactional(readOnly = true)
    public ApplicantApplicationResponse getMyApplication(ApplicationMyRequest request) {
        validateTukoreaEmailAuth(request.email(), request.emailAuthCode(), false);
        MemberApplication application = memberApplicationRepository
                .findFirstByStudentNumberAndEmailOrderBySubmittedAtDesc(
                        request.studentNumber(), request.email())
                .orElseThrow(() -> new BaseException(ErrorCode.APPLICATION_NOT_FOUND));
        return toApplicantResponse(application);
    }

    @Transactional
    public void cancel(String applicationUuid, ApplicationCancelRequest request) {
        validateTukoreaEmailAuth(request.email(), request.emailAuthCode(), false);
        MemberApplication application = memberApplicationRepository.findByApplicationUuid(applicationUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.APPLICATION_NOT_FOUND));
        if (!application.getStudentNumber().equals(request.studentNumber())
                || !application.getEmail().equals(request.email())) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }
        try {
            application.cancel();
        } catch (IllegalStateException e) {
            throw new BaseException(ErrorCode.INVALID_APPLICATION_STATUS);
        }
        redisUtil.deleteData(request.email());
    }

    private void validateTukoreaEmailAuth(String email, String authCode, boolean consumeAuthCode) {
        if (!emailManager.validEmail(email)) {
            throw new BaseException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }
        String storedAuthCode = redisUtil.getData(email);
        if (storedAuthCode == null || !storedAuthCode.equals(authCode)) {
            throw new BaseException(ErrorCode.EMAIL_AUTH_FAILED);
        }
        if (consumeAuthCode) {
            redisUtil.deleteData(email);
        }
    }

    private String resolvePrimaryPortfolioUrl(ApplicationSubmitRequest request) {
        if (request.portfolioUrl() != null && !request.portfolioUrl().isBlank()) {
            return request.portfolioUrl();
        }
        if (request.portfolios() == null || request.portfolios().isEmpty()) {
            return null;
        }
        return request.portfolios().get(0).url();
    }

    private void saveAnswers(MemberApplication application, List<ApplicationSubmitRequest.AnswerRequest> answers) {
        List<ApplicationQuestion> questions = applicationQuestionService
                .getQuestions(application.getGeneration());
        Map<String, ApplicationQuestion> questionByUuid = questions.stream()
                .collect(Collectors.toMap(ApplicationQuestion::getQuestionUuid, Function.identity()));
        Map<String, String> answerByQuestionUuid = answers == null ? Map.of() : answers.stream()
                .collect(Collectors.toMap(
                        ApplicationSubmitRequest.AnswerRequest::questionUuid,
                        ApplicationSubmitRequest.AnswerRequest::answer,
                        (left, right) -> right));

        for (ApplicationQuestion question : questions) {
            String answer = answerByQuestionUuid.get(question.getQuestionUuid());
            if (Boolean.TRUE.equals(question.getIsRequired()) && (answer == null || answer.isBlank())) {
                throw new BaseException(ErrorCode.REQUIRED_ANSWER_MISSING);
            }
        }

        if (answers == null || answers.isEmpty()) {
            return;
        }
        List<ApplicationAnswer> entities = answers.stream()
                .map(answer -> {
                    ApplicationQuestion question = questionByUuid.get(answer.questionUuid());
                    if (question == null) {
                        throw new BaseException(ErrorCode.QUESTION_NOT_FOUND);
                    }
                    return ApplicationAnswer.builder()
                            .applicationId(application.getId())
                            .applicationQuestionId(question.getId())
                            .questionSnapshot(question.getQuestion())
                            .answer(answer.answer())
                            .build();
                })
                .toList();
        applicationAnswerRepository.saveAll(entities);
    }

    private void savePortfolios(MemberApplication application, List<ApplicationSubmitRequest.PortfolioRequest> portfolios) {
        if (portfolios == null || portfolios.isEmpty()) {
            return;
        }
        List<ApplicationPortfolioUrl> entities = portfolios.stream()
                .map(portfolio -> ApplicationPortfolioUrl.builder()
                        .memberApplicationId(application.getId())
                        .label(portfolio.label())
                        .url(portfolio.url())
                        .sortOrder(portfolio.sortOrder() == null ? 0 : portfolio.sortOrder())
                        .build())
                .toList();
        applicationPortfolioUrlRepository.saveAll(entities);
    }

    private ApplicantApplicationResponse toApplicantResponse(MemberApplication application) {
        List<ApplicationDetailResponse.AnswerItem> answers = applicationAnswerRepository
                .findByApplicationId(application.getId()).stream()
                .map(answer -> new ApplicationDetailResponse.AnswerItem(
                        answer.getQuestionSnapshot(), answer.getAnswer()))
                .toList();
        List<ApplicationDetailResponse.PortfolioItem> portfolios = applicationPortfolioUrlRepository
                .findByMemberApplicationIdOrderBySortOrderAsc(application.getId()).stream()
                .map(portfolio -> new ApplicationDetailResponse.PortfolioItem(
                        portfolio.getLabel(), portfolio.getUrl()))
                .toList();
        return ApplicantApplicationResponse.of(application, answers, portfolios);
    }
}
