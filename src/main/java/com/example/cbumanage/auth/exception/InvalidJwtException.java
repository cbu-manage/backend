package com.example.cbumanage.auth.exception;

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
