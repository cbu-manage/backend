package com.example.cbumanage.global.common;

import com.example.cbumanage.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {
	@Value("${cbu.jwt.secret}")
	private String secretKey;
	@Value("${cbu.jwt.expireTime}")
	private Long accessExpireTime;
	@Value("${cbu.jwt.refreshExpireTime}")
	private Long refreshExpireTime;

	private Key key;

	@PostConstruct
	protected void init() {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	public TokenInfo createToken(UUID userUuid, String userName, Role role) {

		Date now = new Date();
		Date accessTokenValidity = new Date(now.getTime() + accessExpireTime);
		String accessToken = Jwts.builder()
				.subject(userUuid.toString())
				.claim("userName", userName)
				.claim("role", role.name())
				.expiration(accessTokenValidity)
				.signWith(key)
				.compact();

		Date refreshTokenValidity = new Date(now.getTime() + refreshExpireTime);
		String refreshToken = Jwts.builder()
				.subject(userUuid.toString())
				.expiration(refreshTokenValidity)
				.signWith(key)
				.compact();

		return new TokenInfo(accessToken, refreshToken);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.verifyWith((SecretKey) key)
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Claims getClaims(String token) {
		return Jwts.parser()
				.verifyWith((SecretKey) key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public Claims getClaimsIgnoreExpiration(String token) {
		try {
			return getClaims(token);
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}
