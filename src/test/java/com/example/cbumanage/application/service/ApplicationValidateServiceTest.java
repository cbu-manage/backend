package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicationValidateRequest;
import com.example.cbumanage.application.dto.ApplicationValidateResponse;
import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.RefSource;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationValidateServiceTest {

    private final MemberApplicationRepository memberApplicationRepository = mock(MemberApplicationRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ApplicationValidateService applicationValidateService =
            new ApplicationValidateService(memberApplicationRepository, userRepository);

    @Test
    void validateReturnsApplicationInfoWhenAcceptedApplicationMatches() {
        Long studentNumber = 2024000001L;
        String nickname = "cbu";
        MemberApplication application = acceptedApplication(studentNumber, nickname);

        when(userRepository.findByStudentNumber(studentNumber)).thenReturn(Optional.empty());
        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.of(application));

        ApplicationValidateResponse response =
                applicationValidateService.validate(new ApplicationValidateRequest(studentNumber, nickname));

        assertThat(response.applicationId()).isEqualTo(application.getId());
        assertThat(response.studentNumber()).isEqualTo(studentNumber);
        assertThat(response.nickName()).isEqualTo(nickname);
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.grade()).isEqualTo(AcademicStatus.JUNIOR.name());
    }

    @Test
    void validateHidesJoinStatusWhenNicknameDoesNotMatch() {
        Long studentNumber = 2024000001L;

        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, "wrong", ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationValidateService.validate(
                new ApplicationValidateRequest(studentNumber, "wrong")))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));

        // 가입 여부를 아예 보지 않아야 학번만 바꿔가며 회원 여부를 훑을 수 없다
        verify(userRepository, never()).findByStudentNumber(studentNumber);
    }

    @Test
    void validateTellsAlreadyJoinedOnlyWhenNicknameMatches() {
        Long studentNumber = 2024000001L;
        String nickname = "cbu";

        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.of(acceptedApplication(studentNumber, nickname)));
        when(userRepository.findByStudentNumber(studentNumber))
                .thenReturn(Optional.of(new User("user@example.com", studentNumber, "encoded-password")));

        assertThatThrownBy(() -> applicationValidateService.validate(
                new ApplicationValidateRequest(studentNumber, nickname)))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ALREADY_JOINED_MEMBER));
    }

    @Test
    void validateRejectsApplicationThatIsNotAdminAccepted() {
        Long studentNumber = 2024000001L;
        String nickname = "cbu";

        when(userRepository.findByStudentNumber(studentNumber)).thenReturn(Optional.empty());
        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationValidateService.validate(
                new ApplicationValidateRequest(studentNumber, nickname)))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));
    }

    @Test
    void validateRequestRequiresStudentNumberAndNickname() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(new ApplicationValidateRequest(null, "")))
                .hasSize(2);
    }

    private static MemberApplication acceptedApplication(Long studentNumber, String nickname) {
        MemberApplication application = MemberApplication.builder()
                .studentNumber(studentNumber)
                .email("applicant@example.com")
                .name("홍길동")
                .nickname(nickname)
                .grade(AcademicStatus.JUNIOR)
                .major("컴퓨터공학과")
                .phoneNumber("010-1234-5678")
                .generation(39L)
                .applicationFields(java.util.Set.of(ApplicationField.DEV))
                .portfolioUrl("https://github.com/cbu")
                .refSource(RefSource.FRIEND)
                .refLinkEtc(null)
                .canOt(true)
                .canWelcome(true)
                .privacyPolicy(true)
                .build();
        application.accept(1L);
        ReflectionTestUtils.setField(application, "id", 10L);
        return application;
    }
}

