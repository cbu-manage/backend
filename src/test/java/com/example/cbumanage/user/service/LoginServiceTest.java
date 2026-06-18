package com.example.cbumanage.user.service;

import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.RefSource;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.global.common.JwtProvider;
import com.example.cbumanage.global.common.TokenInfo;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.global.util.RedisUtil;
import com.example.cbumanage.user.dto.UserLoginRequest;
import com.example.cbumanage.user.dto.UserSignUpRequest;
import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final RedisUtil redisUtil = mock(RedisUtil.class);
    private final MemberApplicationRepository memberApplicationRepository = mock(MemberApplicationRepository.class);
    private final LoginService loginService = new LoginService(
            userRepository,
            jwtProvider,
            redisUtil,
            memberApplicationRepository
    );

    @Test
    void signUpCreatesUserFromAcceptedApplicationAndCompletesApplication() {
        ReflectionTestUtils.setField(loginService, "salt", "test-salt");

        Long studentNumber = 2024000001L;
        String nickname = "cbu";
        MemberApplication application = acceptedApplication(studentNumber, nickname);

        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.of(application));
        when(userRepository.findByStudentNumber(studentNumber)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("applicant@example.com")).thenReturn(Optional.empty());

        loginService.signUp(new UserSignUpRequest(
                "applicant@example.com",
                "password1234",
                "요청 이름은 사용하지 않음",
                studentNumber,
                nickname));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getApplicationId()).isEqualTo(application.getId());
        assertThat(savedUser.getStudentNumber()).isEqualTo(studentNumber);
        assertThat(savedUser.getEmail()).isEqualTo("applicant@example.com");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(savedUser.getMajor()).isEqualTo("컴퓨터공학과");
        assertThat(savedUser.getGrade()).isEqualTo(AcademicStatus.JUNIOR.name());
        assertThat(savedUser.getGeneration()).isEqualTo(39L);
        assertThat(savedUser.getPassword()).isNotEqualTo("password1234");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.COMPLETED);
    }

    @Test
    void signUpRejectsWhenAcceptedApplicationDoesNotExist() {
        Long studentNumber = 2024000001L;
        String nickname = "cbu";

        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.signUp(new UserSignUpRequest(
                "applicant@example.com",
                "password1234",
                "홍길동",
                studentNumber,
                nickname)))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signUpRejectsDuplicateStudentNumber() {
        Long studentNumber = 2024000001L;
        String nickname = "cbu";
        MemberApplication application = acceptedApplication(studentNumber, nickname);

        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.of(application));
        when(userRepository.findByStudentNumber(studentNumber))
                .thenReturn(Optional.of(new User("joined@example.com", studentNumber, "encoded-password")));

        assertThatThrownBy(() -> loginService.signUp(new UserSignUpRequest(
                "applicant@example.com",
                "password1234",
                "홍길동",
                studentNumber,
                nickname)))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ALREADY_JOINED_MEMBER));

        verify(userRepository, never()).save(any(User.class));
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ADMIN_ACCEPTED);
    }

    @Test
    void signUpRejectsDuplicateEmail() {
        Long studentNumber = 2024000001L;
        String nickname = "cbu";
        MemberApplication application = acceptedApplication(studentNumber, nickname);

        when(memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED))
                .thenReturn(Optional.of(application));
        when(userRepository.findByStudentNumber(studentNumber)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("applicant@example.com"))
                .thenReturn(Optional.of(new User("applicant@example.com", 2024999999L, "encoded-password")));

        assertThatThrownBy(() -> loginService.signUp(new UserSignUpRequest(
                "applicant@example.com",
                "password1234",
                "홍길동",
                studentNumber,
                nickname)))
                .isInstanceOfSatisfying(BaseException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE));

        verify(userRepository, never()).save(any(User.class));
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ADMIN_ACCEPTED);
    }

    @Test
    void loginReturnsUserIdInResult() {
        String salt = "test-salt";
        ReflectionTestUtils.setField(loginService, "salt", salt);
        ReflectionTestUtils.setField(loginService, "refreshExpireTime", 864000000L);

        Long userId = 7L;
        Long studentNumber = 2024000001L;
        UUID userUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = new User("admin@example.com", studentNumber, hashedPassword("password1234", salt));
        user.changeRole(Role.ROLE_ADMIN);
        user.updateProfile("관리자", null, "컴퓨터공학과", "4학년", 39L);
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "userUuid", userUuid);

        when(userRepository.findByStudentNumberAndDeletedAtIsNull(studentNumber)).thenReturn(Optional.of(user));
        when(jwtProvider.createToken(userUuid, String.valueOf(studentNumber), Role.ROLE_ADMIN))
                .thenReturn(new TokenInfo("access-token", "refresh-token"));

        LoginService.LoginResult result = loginService.login(new UserLoginRequest(studentNumber, "password1234"));

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo("관리자");
        assertThat(result.email()).isEqualTo("admin@example.com");
        assertThat(result.role()).isEqualTo(Role.ROLE_ADMIN.name());
        verify(redisUtil).setDataExpire("refresh:" + userId, "refresh-token", 864000L);
    }

    @Test
    void deleteUserInvalidatesRefreshTokenAndSoftDeletesUser() {
        Long userId = 1L;
        User user = new User("user@example.com", 20240001L, "encoded-password");
        when(userRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        loginService.deleteUser(userId);

        verify(redisUtil).deleteData("refresh:" + userId);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getMemberStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        verify(userRepository, never()).delete(any(User.class));
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
                .applicationField(ApplicationField.PROJECT)
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

    private static String hashedPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}


