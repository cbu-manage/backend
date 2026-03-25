package com.example.cbumanage.auth.exception;

import com.example.cbumanage.member.exception.MemberException;

public class MemberExistException extends MemberException {
	public MemberExistException() {
	}

	public MemberExistException(String message) {
		super(message);
	}
}
