package com.example.cbumanage.authentication.entity;

import com.example.cbumanage.utils.UUIDProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * RefreshToken 엔티티는 사용자의 리프레시 토큰 정보를 저장하는 JPA 엔티티입니다.
 * 이 토큰은 사용자의 로그인 상태를 유지하기 위해 사용됩니다.
 */
@Entity
@Data                        // Lombok 어노테이션: getter, setter, toString, equals, hashCode 메서드 자동 생성
@NoArgsConstructor         // 기본 생성자 자동 생성
@AllArgsConstructor        // 모든 필드를 인자로 받는 생성자 자동 생성
public class RefreshToken {

	/**
	 * RefreshToken의 고유 식별자.
	 * UUID를 BINARY(16) 형식으로 데이터베이스에 저장합니다.
	 */
	@Id
	@Column(columnDefinition = "BINARY(16)")
	private UUID id;

	/**
	 * 해당 RefreshToken이 속한 사용자의 ID.
	 */
	private Long userId;

	/**
	 * RefreshToken의 만료 시간(예: Unix 타임스탬프 형식).
	 */
	private Long exp;

	/**
	 * 사용자 ID와 만료 시간(exp)을 기반으로 RefreshToken 객체를 생성하는 생성자.
	 * UUID는 UUIDProvider.random() 메서드를 사용해 생성합니다.
	 *
	 * @param userId 해당 토큰이 속한 사용자의 ID
	 * @param exp 토큰의 만료 시간(예: Unix 타임스탬프)
	 */
	public RefreshToken(Long userId, Long exp) {
		this.id = UUIDProvider.random(); // UUIDProvider를 통해 새로운 UUID 생성
		this.userId = userId;
		this.exp = exp;
	}
}
