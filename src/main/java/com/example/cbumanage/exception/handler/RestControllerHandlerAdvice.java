package com.example.cbumanage.exception.handler;

import com.example.cbumanage.authentication.exceptions.handler.ExceptionMessage;
import com.example.cbumanage.exception.CustomException;
import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.response.ResultResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ResultResponse<Void>> handleCustomException(CustomException e) {
		return ResultResponse.error(e.getErrorCode(), e.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ResultResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		return ResultResponse.error(ErrorCode.INVALID_REQUEST, "데이터 무결성 제약 조건에 위배됩니다.");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResultResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.findFirst()
				.orElse("잘못된 요청입니다.");
		return ResultResponse.error(ErrorCode.INVALID_REQUEST, message);
	}
}
