package com.example.cbumanage.exception.handler;

import com.example.cbumanage.authentication.exceptions.handler.ExceptionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestControllerHandlerAdvice {


	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionMessage runtimeException(RuntimeException e) {
		e.printStackTrace();
		return new ExceptionMessage(e.getClass().getName(), e.getMessage(), 500);
	}

	public ExceptionMessage runtimeException(RuntimeException e, Integer status) {
		return new ExceptionMessage(e.getClass().getName(), e.getMessage(), status);
	}

	// @Valid 검증 실패 시 발생하는 예외를 처리하기 위한 핸들러
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionMessage handleValidationException(MethodArgumentNotValidException e) {

		FieldError fieldError = e.getBindingResult().getFieldError();

		String message = (fieldError != null)
				? fieldError.getField() + " : " + fieldError.getDefaultMessage()
				: "Validation error";

		return new ExceptionMessage(
				e.getClass().getSimpleName(), message, 400
		);
	}
}
