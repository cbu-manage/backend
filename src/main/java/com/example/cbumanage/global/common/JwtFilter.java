package com.example.cbumanage.global.common;

import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = httpServletRequest.getCookies();
        String accessToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }
        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            Claims claims = jwtProvider.getClaims(accessToken);
            String uuidStr = claims.getSubject();
            String role = claims.get("role", String.class);
            if (uuidStr != null && role != null) {
                User user = userRepository.findByUserUuidAndDeletedAtIsNull(UUID.fromString(uuidStr)).orElse(null);
                // 권한은 토큰의 role claim이 아니라 DB의 현재 role로 판정한다.
                // claim을 쓰면 역할이 바뀌어도 토큰이 만료될 때까지 이전 권한이 유지된다.
                if (user != null && user.getRole() != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            String.valueOf(user.getUserId()), null,
                            List.of(new SimpleGrantedAuthority(user.getRole().name())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
