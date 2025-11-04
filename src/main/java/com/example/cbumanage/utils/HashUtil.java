package com.example.cbumanage.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HashUtil {
	public String hash(String hash) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] encodedhash = digest.digest(hash.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encodedhash);
	}
}
