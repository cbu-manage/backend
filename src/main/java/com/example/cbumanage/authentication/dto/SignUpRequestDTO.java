package com.example.cbumanage.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 회원가입 요청 시 클라이언트로부터 전달받는 데이터 전송 객체(DTO)입니다.
 * 각 필드는 유효성 검증 어노테이션을 통해 입력 값의 조건을 지정합니다.
 */
@Data
public class SignUpRequestDTO {

	/**
	 * 사용자의 이메일 주소.
	 * - @Email: 올바른 이메일 형식이어야 합니다.
	 * - @NotNull, @NotEmpty: null이나 빈 문자열이 허용되지 않습니다.
	 */
	@Email
	private String email;

	/**
	 * 사용자의 비밀번호.
	 * - @Size(min = 8): 최소 8자 이상이어야 합니다.
	 * - @NotNull, @NotEmpty: null이나 빈 문자열이 허용되지 않습니다.
	 */
	@Size(min = 8)
	@NotNull
	@NotEmpty
	private String password;

	/**
	 * 사용자의 이름.
	 * - @Size(min = 2): 최소 2자 이상이어야 합니다.
	 * - @NotNull, @NotEmpty: null이나 빈 문자열이 허용되지 않습니다.
	 */
	@NotNull
	@NotEmpty
	@Size(min = 2)
	private String name;

	/**
	 * 사용자의 학번.
	 * - @NotNull: 반드시 값이 있어야 합니다.
	 */
	@NotNull
	private Long studentNumber;

	/**
	 * 사용자의 닉네임.
	 * - @Size(min = 3): 최소 3자 이상이어야 합니다.
	 * - @NotNull, @NotEmpty: null이나 빈 문자열이 허용되지 않습니다.
	 */
	@NotNull
	@NotEmpty
	@Size(min = 3)
	private String nickname;
}