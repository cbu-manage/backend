package com.example.cbumanage.authentication.exceptions.handler;

import com.example.cbumanage.authentication.exceptions.AuthenticationException;
import com.example.cbumanage.authentication.exceptions.InvalidEmailException;
import com.example.cbumanage.authentication.exceptions.InvalidJwtException;
import com.example.cbumanage.exception.MemberException;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.exception.handler.RestControllerHandlerAdvice;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * LoginControllerAdvice는
 * com.example.cbumanage.authentication.controller 패키지 내 컨트롤러에서 발생하는 예외를
 * 전역적으로 처리하기 위한 @RestControllerAdvice 클래스입니다.
 *
 * 이 클래스는 공통 예외 처리 로직을 제공하는 RestControllerHandlerAdvice를 활용하여
 * 다양한 예외 상황에 맞는 HTTP 응답을 생성합니다.
 */
@RestControllerAdvice(basePackages = "com.example.cbumanage.authentication.controller")
public class LoginControllerAdvice {

	// 공통 예외 처리 로직을 제공하는 핸들러(예외 메시지 포맷팅 등)
	private final RestControllerHandlerAdvice headHandler;

	/**
	 * 생성자.
	 * 외부에서 주입받은 RestControllerHandlerAdvice를 사용하여 공통 예외 처리 메서드를 호출할 수 있도록 초기화합니다.
	 *
	 * @param restControllerHandlerAdvice 공통 예외 처리 핸들러
	 */
	public LoginControllerAdvice(RestControllerHandlerAdvice restControllerHandlerAdvice) {
		this.headHandler = restControllerHandlerAdvice;
	}

	/**
	 * AuthenticationException 예외 처리.
	 * AuthenticationException이 발생하면 HTTP 401 (Unauthorized) 상태 코드와 함께 예외 메시지를 반환합니다.
	 *
	 * @param e 발생한 MemberNotExistsException (실제로 AuthenticationException을 상속한 예외)
	 * @return 공통 예외 처리 핸들러가 생성한 ExceptionMessage 객체
	 */
	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ExceptionMessage memberNotExistsException(MemberNotExistsException e) {
		return headHandler.runtimeException(e, HttpStatus.UNAUTHORIZED.value());
	}

	/**
	 * InvalidJwtException 예외 처리.
	 * 잘못된 JWT 토큰 사용 시 HTTP 401 (Unauthorized) 상태 코드와 함께 예외 메시지를 반환합니다.
	 *
	 * @param e 발생한 InvalidJwtException
	 * @return 공통 예외 처리 핸들러가 생성한 ExceptionMessage 객체
	 */
	@ExceptionHandler(InvalidJwtException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ExceptionMessage invalidJwtException(InvalidJwtException e) {
		return headHandler.runtimeException(e, HttpStatus.UNAUTHORIZED.value());
	}

	/**
	 * MemberException 예외 처리.
	 * 회원 관련 예외 발생 시 HTTP 400 (Bad Request) 상태 코드와 함께 예외 메시지를 반환합니다.
	 *
	 * @param e 발생한 MemberException
	 * @return 공통 예외 처리 핸들러가 생성한 ExceptionMessage 객체
	 */
	@ExceptionHandler(MemberException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionMessage memberException(MemberException e) {
		return headHandler.runtimeException(e, HttpStatus.BAD_REQUEST.value());
	}

	/**
	 * InvalidEmailException 예외 처리.
	 * 잘못된 이메일 형식 등의 이메일 관련 오류가 발생하면 HTTP 400 (Bad Request) 상태 코드와 함께 예외 메시지를 반환합니다.
	 *
	 * @param e 발생한 InvalidEmailException
	 * @return 공통 예외 처리 핸들러가 생성한 ExceptionMessage 객체
	 */
	@ExceptionHandler(InvalidEmailException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionMessage invalidEmailException(InvalidEmailException e) {
		return headHandler.runtimeException(e, HttpStatus.BAD_REQUEST.value());
	}

	/**
	 * ConstraintViolationException 예외 처리.
	 * 유효성 검사 실패 시 발생하는 예외를 HTTP 400 (Bad Request) 상태 코드와 함께 처리합니다.
	 *
	 * @param e 발생한 ConstraintViolationException
	 * @return 공통 예외 처리 핸들러가 생성한 ExceptionMessage 객체
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionMessage constraintViolationException(ConstraintViolationException e) {
		return headHandler.runtimeException(e, HttpStatus.BAD_REQUEST.value());
	}

	/**
	 * TypeMismatchException 예외 처리.
	 * 요청 파라미터 타입이 일치하지 않을 때 발생하는 예외를 HTTP 400 (Bad Request) 상태 코드와 함께 처리합니다.
	 *
	 * @param e 발생한 TypeMismatchException
	 * @return 공통 예외 처리 핸들러가 생성한 ExceptionMessage 객체
	 */
	@ExceptionHandler(TypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionMessage typeMismatchException(TypeMismatchException e) {
		return headHandler.runtimeException(e, HttpStatus.BAD_REQUEST.value());
	}
}
