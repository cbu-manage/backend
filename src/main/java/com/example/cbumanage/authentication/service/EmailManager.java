package com.example.cbumanage.authentication.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class EmailManager {

	private HashSet<String> possibleDomains = new HashSet<>(Set.of("tukorea.ac.kr"));

	public boolean validEmail(final String email) {
		String[] split = email.split("@");
		if (split.length != 2) return false;
		String domain = split[1];
		return possibleDomains.contains(domain);
	}
}
