package com.example.cbumanage.authentication.exceptions;

public class InvalidJwtException extends RuntimeException {
	public InvalidJwtException() {
		super();
	}

	public InvalidJwtException(String message) {
		super(message);
	}

	public InvalidJwtException(String message, Throwable cause) {
		super(message, cause);
	}
}
