package com.example.cbumanage.authentication.exceptions;

import com.example.cbumanage.exception.MemberException;

public class MemberExistException extends MemberException {
	public MemberExistException() {
	}

	public MemberExistException(String message) {
		super(message);
	}
}
