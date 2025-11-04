package com.example.cbumanage.utils;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.val;
import org.hibernate.TypeMismatchException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtProvider {

	private String secretKey;
	private Long expireTime;
	private SecretKey key;
	private MacAlgorithm alg;

	public JwtProvider(@Value("${cbu.jwt.secret}") String secretKey, @Value("${cbu.jwt.expireTime}") Long expireTime) {
		this.secretKey = secretKey;
		this.expireTime = expireTime;
	}

	@PostConstruct
	public void init() {
		alg = Jwts.SIG.HS256;
		key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	public Map<String, Object> parseJwt(final String token, final Map<String, Class<?>> claims) {
		Jws<byte[]> jws = Jwts
				.parser()
				.verifyWith(key)
				.build()
				.parseSignedContent(token);
		JSONObject payload = new JSONObject(new String(jws.getPayload(), StandardCharsets.UTF_8));

		HashMap<String, Object> result = new HashMap<>();
		for (String key : claims.keySet()) {
			Class<?> type = claims.get(key);
			Object value = payload.get(key);
			if (value.getClass().isAssignableFrom(Integer.class) && type == Long.class) {
				result.put(key, payload.getLong(key));
				continue;
			}
			if (type == UUID.class) {
				result.put(key, UUID.fromString(payload.getString(key)));
				continue;
			}
			if (!value.getClass().isAssignableFrom(type)) throw new TypeMismatchException("Cannot cast data (" + value + ", " + value.getClass() + ") to " + type.getName() + " type.");
			if (type.isAssignableFrom(JSONArray.class)) {
				result.put(key, payload.getJSONArray(key));
				continue;
			}
			result.put(key, value);
		}
		return result;
	}

	public String generateJwt(final String type, final Map<String, Object> payload) {
		Map<String, Object> newPayload = new HashMap<>(payload);
		newPayload.putIfAbsent("exp", (currentTime() + this.expireTime));
		newPayload.putIfAbsent("iat", currentTime());

		return Jwts.builder().header().type(type).and()
				.content(new JSONObject(newPayload).toString().getBytes(StandardCharsets.UTF_8), "application/json")
				.signWith(key, alg)
				.compact();
	}

	public long currentTime() {
		return System.currentTimeMillis();
	}
}
