package com.example.cbumanage.api.v2;

import com.example.cbumanage.api.v2.dto.MyInfoV2Response;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.common.TokenInfo;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.user.dto.MyInfoResponse;
import com.example.cbumanage.user.dto.PasswordChangeRequest;
import com.example.cbumanage.user.dto.PasswordResetRequest;
import com.example.cbumanage.user.dto.UserLoginRequest;
import com.example.cbumanage.user.dto.UserLoginResponse;
import com.example.cbumanage.user.dto.UserSignUpRequest;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
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
@RequestMapping("/api/v2/login")
@RequiredArgsConstructor
@Tag(name = "V2 로그인 컨트롤러", description = "UUID 기반 사용자 식별자를 반환하는 로그인 API")
public class LoginV2Controller {
    private final LoginService loginService;
    private final UserRepository userRepository;

    @Value("${cbu.jwt.secureCookie:false}")
    private boolean secureCookie;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "V2 로그인 후 쿠키에 토큰 반환", description = "응답에 userUuid를 포함합니다.")
    public ApiResponse<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        LoginService.LoginResult result = loginService.login(userLoginRequest);
        addTokenCookies(response, result.tokenInfo());
        return ApiResponse.success(new UserLoginResponse(result.userUuid(), result.name(), result.email(), result.role()));
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "V2 내 정보 조회", description = "userId 대신 userUuid를 반환합니다.")
    public ApiResponse<MyInfoV2Response> getMyInfo(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        MyInfoResponse response = loginService.getMyInfo(userId);
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        return ApiResponse.success(MyInfoV2Response.from(response, user.getUserUuid()));
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "V2 토큰 갱신", description = "refreshToken 쿠키로 accessToken 재발급")
    public ApiResponse<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refreshToken");
        TokenInfo newToken = loginService.refresh(refreshToken);
        addTokenCookies(response, newToken);
        return ApiResponse.success();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "V2 로그아웃", description = "refreshToken 무효화 및 쿠키 삭제")
    public ApiResponse<Void> logout(Authentication authentication, HttpServletResponse response) {
        Long userId = Long.parseLong(authentication.getName());
        loginService.logout(userId);
        clearTokenCookies(response);
        return ApiResponse.success();
    }

    @DeleteMapping("/account")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "V2 회원 탈퇴")
    public ApiResponse<Void> deleteUser(Authentication authentication, HttpServletResponse response) {
        Long userId = Long.parseLong(authentication.getName());
        loginService.deleteUser(userId);
        clearTokenCookies(response);
        return ApiResponse.success();
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "V2 회원가입", description = "회원가입 요청 형식은 v1과 동일합니다.")
    public ApiResponse<Void> signUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        loginService.signUp(userSignUpRequest);
        return ApiResponse.success();
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "V2 비밀번호 변경")
    public ApiResponse<Void> changePassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        loginService.changePassword(userId, request);
        return ApiResponse.success();
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "V2 비밀번호 초기화", description = "학번/이메일/인증코드 검증 후 비밀번호를 재설정합니다.")
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
