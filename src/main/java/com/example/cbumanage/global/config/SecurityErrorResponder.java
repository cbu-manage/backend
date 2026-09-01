package com.example.cbumanage.global.config;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 시큐리티 필터 체인에서 끊긴 요청의 응답 본문.
 * 여기서 막히면 @RestControllerAdvice 까지 오지 않아 상태코드만 있고 본문이 비어 나갔고,
 * 그래서 프론트가 "세션 만료"와 "권한 없음"을 구분할 수 없었다.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {

    private final ObjectMapper objectMapper;

    /** 인증 자체가 안 된 경우(토큰 없음·만료·위조) → 401. 프론트는 재로그인으로 유도한다. */
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> write(response, ErrorCode.UNAUTHORIZED);
    }

    /** 인증은 됐지만 권한이 모자란 경우 → 403. 재로그인해도 달라지지 않는다. */
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> write(response, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorCode));
    }
}
