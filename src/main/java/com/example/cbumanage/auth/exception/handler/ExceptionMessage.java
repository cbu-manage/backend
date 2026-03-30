package com.example.cbumanage.auth.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionMessage {
	private String error;
	private String message;
	private Integer status;
}
