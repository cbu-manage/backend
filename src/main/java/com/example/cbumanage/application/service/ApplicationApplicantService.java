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
import com.example.cbumanage.application.entity.Recruitment;
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

import java.time.LocalDate;
import java.util.LinkedHashSet;
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

        Long generation = assertAcceptingAndGetGeneration();
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
                .applicationFields(new LinkedHashSet<>(request.applicationFields()))
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

    /**
     * 접수 가능한지 확인하고 기수를 돌려준다.
     * 진행 중인 모집이 없어도 정책 기수로 폴백해 그냥 접수됐고, 모집 기간 밖에서도 받았다.
     */
    private Long assertAcceptingAndGetGeneration() {
        Recruitment recruitment = recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_ACCEPTING));
        LocalDate today = LocalDate.now();
        if (recruitment.getPlannedStartDate() != null && today.isBefore(recruitment.getPlannedStartDate())) {
            throw new BaseException(ErrorCode.RECRUITMENT_NOT_ACCEPTING);
        }
        if (recruitment.getPlannedEndDate() != null && today.isAfter(recruitment.getPlannedEndDate())) {
            throw new BaseException(ErrorCode.RECRUITMENT_NOT_ACCEPTING);
        }
        return recruitment.getGeneration();
    }

    /* 학번+닉네임만 맞으면 지원서 전문이 나가므로 학번 단위로 시도 횟수를 제한한다 */
    private static final long LOOKUP_WINDOW_SECONDS = 60 * 60L;
    private static final long LOOKUP_MAX_PER_WINDOW = 10L;

    @Transactional(readOnly = true)
    public ApplicantApplicationResponse getMyApplication(ApplicationMyRequest request) {
        String lookupKey = "application:lookup:" + request.studentNumber();
        if (redisUtil.increaseWithExpire(lookupKey, LOOKUP_WINDOW_SECONDS) > LOOKUP_MAX_PER_WINDOW) {
            throw new BaseException(ErrorCode.APPLICATION_LOOKUP_LIMIT_EXCEEDED);
        }
        MemberApplication application = memberApplicationRepository
                .findFirstByStudentNumberAndNicknameOrderBySubmittedAtDesc(
                        request.studentNumber(), request.nickname())
                .orElseThrow(() -> new BaseException(ErrorCode.APPLICATION_NOT_FOUND));
        ApplicantApplicationResponse response = toApplicantResponse(application);
        return isBeforeAnnouncement(application.getGeneration()) ? response.hideResult() : response;
    }

    /** 발표일이 지나지 않았으면 결과를 감춘다. 발표일이 없으면 아직 안 정해진 것으로 본다. */
    private boolean isBeforeAnnouncement(Long generation) {
        return recruitmentRepository.findByGeneration(generation)
                .map(recruitment -> recruitment.getAnnouncementDate() == null
                        || LocalDate.now().isBefore(recruitment.getAnnouncementDate()))
                .orElse(false);
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

    private void saveAnswers(MemberApplication application, Map<String, String> answers) {
        List<ApplicationQuestion> questions = applicationQuestionService
                .getQuestions(application.getGeneration());
        Map<String, ApplicationQuestion> questionByType = questions.stream()
                .collect(Collectors.toMap(ApplicationQuestion::getType, Function.identity()));
        Map<String, String> answerByType = answers == null ? Map.of() : answers;

        // 필수 질문에 답이 있는지 검사 (type 기준)
        for (ApplicationQuestion question : questions) {
            String answer = answerByType.get(question.getType());
            if (Boolean.TRUE.equals(question.getIsRequired()) && (answer == null || answer.isBlank())) {
                throw new BaseException(ErrorCode.REQUIRED_ANSWER_MISSING);
            }
        }

        if (answers == null || answers.isEmpty()) {
            return;
        }

        // 각 (type → 답변)을 질문에 연결해 저장
        List<ApplicationAnswer> entities = answers.entrySet().stream()
                .map(entry -> {
                    ApplicationQuestion question = questionByType.get(entry.getKey());
                    if (question == null) {
                        throw new BaseException(ErrorCode.QUESTION_NOT_FOUND);
                    }
                    return ApplicationAnswer.builder()
                            .applicationId(application.getId())
                            .applicationQuestionId(question.getId())
                            .questionSnapshot(question.getQuestion())
                            .answer(entry.getValue())
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
