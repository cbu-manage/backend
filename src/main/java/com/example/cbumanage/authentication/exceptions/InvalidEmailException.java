package com.example.cbumanage.authentication.exceptions;

public class InvalidEmailException extends RuntimeException {
	public InvalidEmailException() {
	}

	public InvalidEmailException(String message) {
		super(message);
	}
}
