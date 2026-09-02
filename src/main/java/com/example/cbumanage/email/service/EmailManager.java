package com.example.cbumanage.email.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class EmailManager {

	private HashSet<String> possibleDomains = new HashSet<>(Set.of("tukorea.ac.kr"));

	public boolean validEmail(final String email) {
		String[] split = email.split("@");
		if (split.length != 2) return false;
		// 대문자로 적어도 같은 학교 메일이다
		String domain = split[1].toLowerCase();
		return possibleDomains.contains(domain);
	}
}
