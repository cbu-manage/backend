package com.example.cbumanage.user.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.common.TokenInfo;
import com.example.cbumanage.user.dto.MyInfoResponse;
import com.example.cbumanage.user.dto.PasswordChangeRequest;
import com.example.cbumanage.user.dto.PasswordResetRequest;
import com.example.cbumanage.user.dto.UserLoginRequest;
import com.example.cbumanage.user.dto.UserLoginResponse;
import com.example.cbumanage.user.dto.UserSignUpRequest;
import com.example.cbumanage.user.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
@Tag(name = "인증", description = "로그인, 토큰 갱신, 회원가입, 비밀번호 변경을 처리합니다.")
public class LoginController {
    private final LoginService loginService;

    @Value("${cbu.jwt.secureCookie:false}")
    private boolean secureCookie;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "로그인", description = "학번과 비밀번호를 검증하고 accessToken·refreshToken 쿠키를 발급합니다.")
    public ApiResponse<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        LoginService.LoginResult result = loginService.login(userLoginRequest);
        addTokenCookies(response, result.tokenInfo());
        return ApiResponse.success(new UserLoginResponse(result.userId(), result.name(), result.email(), result.role()));
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 기본 정보를 조회합니다.")
    public ApiResponse<MyInfoResponse> getMyInfo(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(loginService.getMyInfo(userId));
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "토큰 갱신", description = "refreshToken 쿠키를 검증하고 accessToken·refreshToken 쿠키를 재발급합니다.")
    public ApiResponse<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refreshToken");
        TokenInfo newToken = loginService.refresh(refreshToken);
        addTokenCookies(response, newToken);
        return ApiResponse.success();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "로그아웃", description = "refreshToken을 무효화하고 인증 쿠키를 삭제합니다.")
    public ApiResponse<Void> logout(Authentication authentication, HttpServletResponse response) {
        Long userId = Long.parseLong(authentication.getName());
        loginService.logout(userId);
        clearTokenCookies(response);
        return ApiResponse.success();
    }

    @DeleteMapping("/account")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "회원 탈퇴", description = "로그인 사용자의 계정을 탈퇴 처리하고 인증 쿠키를 삭제합니다.")
    public ApiResponse<Void> deleteUser(Authentication authentication, HttpServletResponse response) {
        Long userId = Long.parseLong(authentication.getName());
        loginService.deleteUser(userId);
        clearTokenCookies(response);
        return ApiResponse.success();
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "승인된 지원서 정보를 검증한 뒤 사용자 계정을 생성합니다.")
    public ApiResponse<Void> signUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        loginService.signUp(userSignUpRequest);
        return ApiResponse.success();
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.")
    public ApiResponse<Void> changePassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        loginService.changePassword(userId, request);
        return ApiResponse.success();
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "비밀번호 초기화", description = "학번/이메일/인증코드 검증 후 비밀번호를 재설정합니다.")
    public ApiResponse<Void> resetPassword(@RequestBody PasswordResetRequest request) {
        loginService.resetPassword(request);
        return ApiResponse.success();
    }

    private void addTokenCookies(HttpServletResponse response, TokenInfo tokenInfo) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenInfo.accessToken())
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(600)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenInfo.refreshToken())
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(604800)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    private void clearTokenCookies(HttpServletResponse response) {
        ResponseCookie accessClear = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(secureCookie).path("/").maxAge(0).sameSite("Strict").build();
        ResponseCookie refreshClear = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(secureCookie).path("/").maxAge(0).sameSite("Strict").build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessClear.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshClear.toString());
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
