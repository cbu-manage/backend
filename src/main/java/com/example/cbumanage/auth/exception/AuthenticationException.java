package com.example.cbumanage.auth.exception;

public class AuthenticationException extends RuntimeException {
	public AuthenticationException() {
		super("Fail to authentication or authorization");
	}

	public AuthenticationException(String message) {
		super(message);
	}
}
