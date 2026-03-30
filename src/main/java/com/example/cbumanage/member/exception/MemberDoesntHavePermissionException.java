package com.example.cbumanage.member.exception;

public class MemberDoesntHavePermissionException extends MemberException {
	public MemberDoesntHavePermissionException() {
	}

	public MemberDoesntHavePermissionException(String message) {
		super(message);
	}
}
