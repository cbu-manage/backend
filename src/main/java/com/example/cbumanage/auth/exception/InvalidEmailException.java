package com.example.cbumanage.auth.exception;

public class InvalidEmailException extends RuntimeException {
	public InvalidEmailException() {
	}

	public InvalidEmailException(String message) {
		super(message);
	}
}
