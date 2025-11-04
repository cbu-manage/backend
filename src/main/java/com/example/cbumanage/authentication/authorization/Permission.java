package com.example.cbumanage.authentication.authorization;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 사용자 권한(Permission)을 정의하는 열거형(enum)입니다.
 * 각 Permission은 특정 경로(path)와 제외 경로(exclusivePath)를 가질 수 있습니다.
 */
public enum Permission {

	/**
	 * 일반 회원 권한.
	 * - path: 인증이 필요한 경로
	 * - exclusivePath: 인증이 필요하지 않은 예외 경로
	 */
	MEMBER(
			Set.of("/api/v1/*"),     // 인증이 필요한 경로
			Set.of("/api/v1/login")  // 인증이 필요하지 않은 경로
	),

	/**
	 * 관리자 권한.
	 * - path: 아직 설정되지 않음
	 * - exclusivePath: 아직 설정되지 않음
	 */
	ADMIN(
			Set.of(), // 인증이 필요한 경로(비어 있음)
			Set.of("/api/*")  // 인증이 필요하지 않은 경로(비어 있음)
	);

	@Getter
	private Set<String> path;          // 인증이 필요한 경로 목록
	@Getter
	private Set<String> exclusivePath; // 인증에서 제외할 경로 목록
	@Getter
	private String name;               // 열거형 이름을 소문자로 변환하여 저장

	/**
	 * Permission 생성자.
	 * path와 exclusivePath, 그리고 name(열거형 이름)을 설정합니다.
	 */
	Permission(Set<String> path, Set<String> exclusivePath, String name) {
		this.path = path;
		this.exclusivePath = exclusivePath;
		this.name = name;
	}

	/**
	 * Permission 생성자.
	 * path와 exclusivePath는 매개변수로 받고,
	 * name은 this.name().toLowerCase()로 자동 설정합니다.
	 */
	Permission(Set<String> path, Set<String> exclusivePath) {
		this.path = path;
		this.exclusivePath = exclusivePath;
		this.name = this.name().toLowerCase();
	}

	/**
	 * toString() 메서드를 오버라이드하여,
	 * 열거형의 실제 상수 이름이 아닌 소문자 name을 반환하도록 합니다.
	 */
	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * 문자열로부터 Permission 열거형 값을 얻기 위해 사용되는 Map.
	 * key는 소문자 name, value는 Permission 객체입니다.
	 */
	private static Map<String, Permission> permissionMap = init();

	/**
	 * permissionMap을 초기화하는 메서드.
	 * 각 Permission 상수의 name을 key로 하여 Map에 저장합니다.
	 */
	private static Map<String, Permission> init() {
		HashMap<String, Permission> result = new HashMap<>();
		for (Permission value : Permission.values()) {
			result.put(value.name, value);
		}
		return result;
	}

	/**
	 * 문자열 name에 해당하는 Permission 열거형 값을 반환합니다.
	 * 만약 해당 name이 존재하지 않으면 null을 반환할 수 있습니다.
	 */
	public static Permission getValue(String name) {
		return permissionMap.get(name);
	}
}
