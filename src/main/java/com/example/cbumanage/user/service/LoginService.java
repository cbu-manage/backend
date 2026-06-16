package com.example.cbumanage.user.service;

import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.global.common.JwtProvider;
import com.example.cbumanage.global.common.TokenInfo;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.global.util.RedisUtil;
import com.example.cbumanage.user.dto.MyInfoResponse;
import com.example.cbumanage.user.dto.PasswordChangeRequest;
import com.example.cbumanage.user.dto.PasswordResetRequest;
import com.example.cbumanage.user.dto.UserLoginRequest;
import com.example.cbumanage.user.dto.UserSignUpRequest;
import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;
    private final MemberApplicationRepository memberApplicationRepository;

    private static final String REFRESH_KEY_PREFIX = "refresh:";

    @Value("${cbu.login.salt}")
    private String salt;

    @Value("${cbu.jwt.refreshExpireTime}")
    private Long refreshExpireTime;

    @Transactional
    public void signUp(UserSignUpRequest request) {
        MemberApplication application = findAcceptedApplication(request.studentNumber(), request.nickname());

        userRepository.findByStudentNumber(request.studentNumber()).ifPresent(u -> {
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        });
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new BaseException(ErrorCode.DUPLICATE_RESOURCE);
        });
        if (!application.getEmail().equals(request.email())) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        User user = new User(
                application.getId(),
                application.getStudentNumber(),
                hashPassword(request.password()),
                request.email(),
                application.getName(),
                application.getPhoneNumber(),
                application.getMajor(),
                application.getGrade().name(),
                application.getGeneration()
        );
        userRepository.save(user);
        application.completeRegistration();
    }

    private MemberApplication findAcceptedApplication(Long studentNumber, String nickname) {
        return memberApplicationRepository.findByStudentNumberAndNicknameAndStatus(
                        studentNumber, nickname, ApplicationStatus.ADMIN_ACCEPTED)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));
    }

    public LoginResult login(UserLoginRequest request) {
        User user = userRepository.findByStudentNumberAndDeletedAtIsNull(request.studentNumber())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!hashPassword(request.password()).equals(user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
        }
        if (user.getRole() == Role.ROLE_USER && user.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new BaseException(ErrorCode.MEMBER_NOT_APPROVED);
        }

        TokenInfo tokenInfo = jwtProvider.createToken(
                user.getUserUuid(),
                String.valueOf(user.getStudentNumber()),
                user.getRole()
        );

        redisUtil.setDataExpire(
                REFRESH_KEY_PREFIX + user.getUserId(),
                tokenInfo.refreshToken(),
                refreshExpireTime / 1000
        );

        return new LoginResult(tokenInfo, user.getName(), user.getEmail(), user.getRole().name());
    }

    public record LoginResult(TokenInfo tokenInfo, String name, String email, String role) {}

    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return new MyInfoResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStudentNumber(),
                user.getMajor(),
                user.getGrade(),
                user.getGeneration()
        );
    }

    public TokenInfo refresh(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        Claims claims = jwtProvider.getClaims(refreshToken);
        UUID userUuid = UUID.fromString(claims.getSubject());
        User user = userRepository.findByUserUuidAndDeletedAtIsNull(userUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        String storedToken = redisUtil.getData(REFRESH_KEY_PREFIX + user.getUserId());
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        TokenInfo newToken = jwtProvider.createToken(
                user.getUserUuid(),
                String.valueOf(user.getStudentNumber()),
                user.getRole()
        );

        redisUtil.setDataExpire(
                REFRESH_KEY_PREFIX + user.getUserId(),
                newToken.refreshToken(),
                refreshExpireTime / 1000
        );

        return newToken;
    }

    public void logout(Long userId) {
        redisUtil.deleteData(REFRESH_KEY_PREFIX + userId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        redisUtil.deleteData(REFRESH_KEY_PREFIX + userId);
        user.delete();
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!hashPassword(request.currentPassword()).equals(user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(hashPassword(request.newPassword()));
        redisUtil.deleteData(REFRESH_KEY_PREFIX + userId);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (request.studentNumber() == null || request.email() == null
                || request.authCode() == null || request.newPassword() == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findByStudentNumberAndDeletedAtIsNull(request.studentNumber())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (user.getEmail() == null || !user.getEmail().equals(request.email())) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        String storedAuthCode = redisUtil.getData(request.email());
        if (storedAuthCode == null || !storedAuthCode.equals(request.authCode())) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        user.changePassword(hashPassword(request.newPassword()));
        redisUtil.deleteData(REFRESH_KEY_PREFIX + user.getUserId());
        redisUtil.deleteData(request.email());
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
