package com.example.cbumanage.user.service;

import com.example.cbumanage.candidate.entity.SuccessCandidate;
import com.example.cbumanage.candidate.repository.SuccessCandidateRepository;
import com.example.cbumanage.global.common.JwtProvider;
import com.example.cbumanage.global.common.TokenInfo;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.global.util.RedisUtil;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.user.dto.MyInfoResponse;
import com.example.cbumanage.user.dto.PasswordChangeRequest;
import com.example.cbumanage.user.dto.UserLoginRequest;
import com.example.cbumanage.user.dto.UserSignUpRequest;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final JwtProvider jwtProvider;
    private final SuccessCandidateRepository successCandidateRepository;
    private final RedisUtil redisUtil;

    private static final String REFRESH_KEY_PREFIX = "refresh:";

    @Value("${cbu.login.salt}")
    private String salt;

    @Value("${cbu.jwt.refreshExpireTime}")
    private Long refreshExpireTime;

    @Transactional
    public void signUp(UserSignUpRequest request) {
        SuccessCandidate candidate = successCandidateRepository.findByStudentNumber(request.studentNumber());
        if (candidate == null) {
            throw new BaseException(ErrorCode.SUCCESS_MEMBER_NOT_FOUND);
        }

        userRepository.findByStudentNumber(request.studentNumber()).ifPresent(u -> {
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        });

        User user = new User(
                request.email(),
                request.studentNumber(),
                hashPassword(request.password())
        );
        userRepository.save(user);

        CbuMember member = new CbuMember();
        member.setName(candidate.getName());
        member.setStudentNumber(candidate.getStudentNumber());
        member.setPhoneNumber(candidate.getPhoneNumber());
        member.setMajor(candidate.getMajor());
        member.setGrade(candidate.getGrade());
        member.setRole(List.of(com.example.cbumanage.member.entity.enums.Role.MEMBER));
        cbuMemberRepository.save(member);
    }

    public LoginResult login(UserLoginRequest request) {
        User user = userRepository.findByStudentNumber(request.studentNumber())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!hashPassword(request.password()).equals(user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
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

        String name = cbuMemberRepository.findNameByStudentNumber(user.getStudentNumber());
        return new LoginResult(tokenInfo, name, user.getEmail(), user.getRole().name());
    }

    public record LoginResult(TokenInfo tokenInfo, String name, String email, String role) {}

    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        CbuMember member = cbuMemberRepository.findByStudentNumber(user.getStudentNumber())
                .orElse(null);

        return new MyInfoResponse(
                user.getUserId(),
                member != null ? member.getName() : null,
                user.getEmail(),
                user.getRole().name(),
                user.getStudentNumber(),
                member != null ? member.getMajor() : null,
                member != null ? member.getGrade() : null,
                member != null ? member.getGeneration() : null
        );
    }

    public TokenInfo refresh(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        Claims claims = jwtProvider.getClaims(refreshToken);
        UUID userUuid = UUID.fromString(claims.getSubject());
        User user = userRepository.findByUserUuid(userUuid)
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        redisUtil.deleteData(REFRESH_KEY_PREFIX + userId);
        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!hashPassword(request.currentPassword()).equals(user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(hashPassword(request.newPassword()));
        redisUtil.deleteData(REFRESH_KEY_PREFIX + userId);
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
