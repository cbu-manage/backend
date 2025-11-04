package com.example.cbumanage.authentication.exceptions;

public class InvalidPasswordException extends AuthenticationException {
	public InvalidPasswordException() {
		super("Invalid password");
	}

	public InvalidPasswordException(String message) {
		super(message);
	}
}
