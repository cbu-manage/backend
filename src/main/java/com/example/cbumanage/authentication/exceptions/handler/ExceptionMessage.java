package com.example.cbumanage.authentication.exceptions.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionMessage {
	private String error;
	private String message;
	private Integer status;
}
