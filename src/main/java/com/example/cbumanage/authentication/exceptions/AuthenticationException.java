package com.example.cbumanage.authentication.exceptions;

public class AuthenticationException extends RuntimeException {
	public AuthenticationException() {
		super("Fail to authentication or authorization");
	}

	public AuthenticationException(String message) {
		super(message);
	}
}
