package com.example.cbumanage.auth.exception;

public class InvalidPasswordException extends AuthenticationException {
	public InvalidPasswordException() {
		super("Invalid password");
	}

	public InvalidPasswordException(String message) {
		super(message);
	}
}
