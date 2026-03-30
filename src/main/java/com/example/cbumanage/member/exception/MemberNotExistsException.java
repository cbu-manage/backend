package com.example.cbumanage.member.exception;

public class MemberNotExistsException extends MemberException {
	public MemberNotExistsException() {
		super("Member isn't exist");
	}

	public MemberNotExistsException(String message) {
		super(message);
	}
}
