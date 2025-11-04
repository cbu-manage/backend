package com.example.cbumanage.utils;

import io.jsonwebtoken.security.SignatureException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class JwtProviderTest {

	private static JwtProvider jwtProvider = new JwtProvider("1234567890123456789012345678901234567890", 86400000L);

	@BeforeAll
	static void init() {
		jwtProvider.init();
	}


	@Test
	void createAndVerifyJWT() {
		// create
		Map<String, Object> value = Map.of("id", 123, "name", "가나다");
		String jwt = jwtProvider.generateJwt("JWT", value);

		// parse jwt
		Map<String, Object> stringObjectMap = jwtProvider.parseJwt(jwt, Map.of("id", Integer.class, "name", String.class));

		// test
		for (String key : value.keySet()) {
			Assertions.assertEquals(stringObjectMap.get(key), value.get(key));
			Assertions.assertEquals(stringObjectMap.get(key).getClass(), value.get(key).getClass());
		}
	}

	@Test
	void invalidTwt() {
		// create
		Map<String, Object> value = Map.of("id", 123, "permission", "user");
		String jwt = jwtProvider.generateJwt("JWT", value);

		// manipulate
		int start = jwt.indexOf('.'), end = jwt.lastIndexOf('.');
		JSONObject payload = new JSONObject(Base64.getDecoder().decode(jwt.substring(start+1, end)));
		payload.put("permission", "admin");
		jwt = jwt.substring(0, start+1)
				+ Base64.getEncoder().encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8))
				+ jwt.substring(end);

		// parse jwt (error)
		try {
			jwtProvider.parseJwt(jwt, Map.of("id", Integer.class, "name", String.class));
		} catch (SignatureException e) {return;}
		Assertions.fail("succeed parsing invalid jwt");
	}

	@Test
	void listJwt() {
		// create
		Map<String, Object> value = Map.of("id", 123, "name", List.of("가나다"));
		String jwt = jwtProvider.generateJwt("JWT", value);
		System.out.println(jwt);

		// parse jwt
		Map<String, Object> stringObjectMap = jwtProvider.parseJwt(jwt, Map.of("id", Integer.class, "name", JSONArray.class));

//		// test
//		for (String key : value.keySet()) {
//			Assertions.assertEquals(stringObjectMap.get(key), value.get(key));
//			Assertions.assertEquals(stringObjectMap.get(key).getClass(), value.get(key).getClass());
//		}
	}
}
