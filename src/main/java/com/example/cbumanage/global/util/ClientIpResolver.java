package com.example.cbumanage.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 요청을 보낸 사람을 식별할 IP를 고른다.
 *
 * 프론트가 BFF로 프록시하므로 실제 사용자의 요청은 백엔드에 프록시 서버 IP 하나로 도착한다.
 * 그래서 BFF가 브라우저 IP를 헤더로 넘기는데, 헤더는 누구나 붙일 수 있으므로
 * 양쪽이 나눠 가진 비밀값이 맞을 때만 신뢰한다.
 * BFF를 거치지 않은 직접 호출은 nginx가 덮어쓰는 X-Real-IP를 쓴다.
 */
@Component
public class ClientIpResolver {

    private static final String CLIENT_IP_HEADER = "X-Client-Ip";
    private static final String PROXY_SECRET_HEADER = "X-Proxy-Secret";
    private static final String REAL_IP_HEADER = "X-Real-IP";

    @Value("${cbu.proxy.secret:}")
    private String proxySecret;

    /**
     * 식별할 수 없으면 null. 비밀값이 설정되지 않은 동안에는 BFF 경유 요청과 직접 호출을
     * 구분할 수 없고, 그 상태로 IP 제한을 걸면 모든 사용자가 프록시 IP 하나로 묶여
     * 정상 사용자까지 막히므로 제한을 걸지 않는다.
     */
    public String resolve(HttpServletRequest request) {
        if (proxySecret == null || proxySecret.isBlank()) {
            return null;
        }
        if (isFromTrustedProxy(request)) {
            String clientIp = request.getHeader(CLIENT_IP_HEADER);
            if (clientIp != null && !clientIp.isBlank()) {
                return clientIp.trim();
            }
        }
        String realIp = request.getHeader(REAL_IP_HEADER);
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isFromTrustedProxy(HttpServletRequest request) {
        String presented = request.getHeader(PROXY_SECRET_HEADER);
        if (presented == null) {
            return false;
        }
        return MessageDigest.isEqual(
                presented.getBytes(StandardCharsets.UTF_8),
                proxySecret.getBytes(StandardCharsets.UTF_8));
    }
}
