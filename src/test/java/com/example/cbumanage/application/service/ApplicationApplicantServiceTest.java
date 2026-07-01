package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicationCancelRequest;
import com.example.cbumanage.application.dto.ApplicationSubmitRequest;
import com.example.cbumanage.application.entity.ApplicationQuestion;
import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.RefSource;
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
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationApplicantServiceTest {

    private final RecruitmentRepository recruitmentRepository = mock(RecruitmentRepository.class);
    private final MemberApplicationRepository memberApplicationRepository = mock(MemberApplicationRepository.class);
    private final ApplicationAnswerRepository applicationAnswerRepository = mock(ApplicationAnswerRepository.class);
    private final ApplicationPortfolioUrlRepository applicationPortfolioUrlRepository =
            mock(ApplicationPortfolioUrlRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RedisUtil redisUtil = mock(RedisUtil.class);
    private final ApplicationQuestionService applicationQuestionService = mock(ApplicationQuestionService.class);
    private final EmailManager emailManager = mock(EmailManager.class);
    private final RecruitmentGenerationPolicy generationPolicy = new RecruitmentGenerationPolicy(
            Clock.fixed(Instant.parse("2026-06-17T00:00:00Z"), ZoneId.of("Asia/Seoul")),
            2026,
            RecruitmentGenerationPolicy.RecruitmentSeason.SUMMER_BREAK,
            29);
    private final ApplicationApplicantService applicationApplicantService = new ApplicationApplicantService(
            recruitmentRepository,
            memberApplicationRepository,
            applicationAnswerRepository,
            applicationPortfolioUrlRepository,
            userRepository,
            redisUtil,
            applicationQuestionService,
            emailManager,
            generationPolicy
    );

    @Test
    void submitDoesNotConsumeEmailAuthCodeWhenRequiredAnswerIsMissing() {
        ApplicationQuestion question = requiredQuestion("몰입 경험", 1);
        Recruitment recruitment = Recruitment.open(40L, 3);
        when(emailManager.validEmail("applicant@tukorea.ac.kr")).thenReturn(true);
        when(redisUtil.getData("applicant@tukorea.ac.kr")).thenReturn("123456");
        when(recruitmentRepository.findFirstByStatus(recruitment.getStatus())).thenReturn(Optional.of(recruitment));
        when(memberApplicationRepository.findByStudentNumberAndGeneration(2024000001L, 40L))
                .thenReturn(Optional.empty());
        when(memberApplicationRepository.save(org.mockito.ArgumentMatchers.any(MemberApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationQuestionService.getQuestions(40L)).thenReturn(List.of(question));

        assertThatThrownBy(() -> applicationApplicantService.submit(submitRequest(List.of())))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getErrorCode())
                                .isEqualTo(ErrorCode.REQUIRED_ANSWER_MISSING));

        verify(redisUtil, never()).deleteData("applicant@tukorea.ac.kr");
    }

    @Test
    void submitConsumesEmailAuthCodeAfterSuccessfulSubmission() {
        ApplicationQuestion question = requiredQuestion("몰입 경험", 1);
        Recruitment recruitment = Recruitment.open(40L, 3);
        when(emailManager.validEmail("applicant@tukorea.ac.kr")).thenReturn(true);
        when(redisUtil.getData("applicant@tukorea.ac.kr")).thenReturn("123456");
        when(recruitmentRepository.findFirstByStatus(recruitment.getStatus())).thenReturn(Optional.of(recruitment));
        when(memberApplicationRepository.findByStudentNumberAndGeneration(2024000001L, 40L))
                .thenReturn(Optional.empty());
        when(memberApplicationRepository.save(org.mockito.ArgumentMatchers.any(MemberApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationQuestionService.getQuestions(40L)).thenReturn(List.of(question));
        when(applicationAnswerRepository.findByApplicationId(null)).thenReturn(List.of());
        when(applicationPortfolioUrlRepository.findByMemberApplicationIdOrderBySortOrderAsc(null))
                .thenReturn(List.of());

        applicationApplicantService.submit(submitRequest(List.of(
                new ApplicationSubmitRequest.AnswerRequest(question.getQuestionUuid(), "답변입니다.")
        )));

        verify(redisUtil).deleteData("applicant@tukorea.ac.kr");
    }

    @Test
    void submitUsesSeasonGenerationWhenRecruitmentIsNotOpened() {
        ApplicationQuestion question = requiredQuestion("몰입 경험", 1);
        when(emailManager.validEmail("applicant@tukorea.ac.kr")).thenReturn(true);
        when(redisUtil.getData("applicant@tukorea.ac.kr")).thenReturn("123456");
        when(recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)).thenReturn(Optional.empty());
        when(memberApplicationRepository.findByStudentNumberAndGeneration(2024000001L, 29L))
                .thenReturn(Optional.empty());
        when(memberApplicationRepository.save(org.mockito.ArgumentMatchers.any(MemberApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationQuestionService.getQuestions(29L)).thenReturn(List.of(question));
        when(applicationAnswerRepository.findByApplicationId(null)).thenReturn(List.of());
        when(applicationPortfolioUrlRepository.findByMemberApplicationIdOrderBySortOrderAsc(null))
                .thenReturn(List.of());

        var response = applicationApplicantService.submit(submitRequest(List.of(
                new ApplicationSubmitRequest.AnswerRequest(question.getQuestionUuid(), "답변입니다.")
        )));

        assertThat(response.generation()).isEqualTo(29L);
    }

    @Test
    void cancelChangesSubmittedApplicationToCancelledAfterOwnerEmailAuth() {
        MemberApplication application = submittedApplication();
        when(emailManager.validEmail("applicant@tukorea.ac.kr")).thenReturn(true);
        when(redisUtil.getData("applicant@tukorea.ac.kr")).thenReturn("123456");
        when(memberApplicationRepository.findByApplicationUuid(application.getApplicationUuid()))
                .thenReturn(Optional.of(application));

        applicationApplicantService.cancel(
                application.getApplicationUuid(),
                new ApplicationCancelRequest(2024000001L, "applicant@tukorea.ac.kr", "123456"));

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(application.getCancelledAt()).isNotNull();
        verify(redisUtil).deleteData("applicant@tukorea.ac.kr");
    }

    @Test
    void cancelRejectsWhenRequesterIsNotApplicationOwner() {
        MemberApplication application = submittedApplication();
        when(emailManager.validEmail("other@tukorea.ac.kr")).thenReturn(true);
        when(redisUtil.getData("other@tukorea.ac.kr")).thenReturn("123456");
        when(memberApplicationRepository.findByApplicationUuid(application.getApplicationUuid()))
                .thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationApplicantService.cancel(
                application.getApplicationUuid(),
                new ApplicationCancelRequest(2024000002L, "other@tukorea.ac.kr", "123456")))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        verify(redisUtil, never()).deleteData("other@tukorea.ac.kr");
    }

    private ApplicationSubmitRequest submitRequest(List<ApplicationSubmitRequest.AnswerRequest> answers) {
        return new ApplicationSubmitRequest(
                2024000001L,
                "applicant@tukorea.ac.kr",
                "123456",
                "홍길동",
                "cbu",
                AcademicStatus.JUNIOR,
                "컴퓨터공학과",
                "010-1234-5678",
                ApplicationField.DEV,
                null,
                RefSource.FRIEND,
                null,
                true,
                true,
                true,
                answers,
                List.of()
        );
    }

    private ApplicationQuestion requiredQuestion(String question, Integer sortOrder) {
        return ApplicationQuestion.builder()
                .generation(40L)
                .question(question)
                .isRequired(true)
                .sortOrder(sortOrder)
                .build();
    }

    private MemberApplication submittedApplication() {
        return MemberApplication.builder()
                .studentNumber(2024000001L)
                .email("applicant@tukorea.ac.kr")
                .name("홍길동")
                .nickname("cbu")
                .grade(AcademicStatus.JUNIOR)
                .major("컴퓨터공학과")
                .phoneNumber("010-1234-5678")
                .generation(40L)
                .applicationField(ApplicationField.DEV)
                .refSource(RefSource.FRIEND)
                .canOt(true)
                .canWelcome(true)
                .privacyPolicy(true)
                .build();
    }
}
