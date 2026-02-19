package com.example.cbumanage.authentication.entity;

import com.example.cbumanage.authentication.authorization.Permission;
import com.example.cbumanage.authentication.entity.converter.PermissionConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LoginEntity는 사이트 회원(로그인 사용자)의 정보를 저장하는 JPA 엔티티 클래스입니다.
 * 데이터베이스 테이블 "site_member"와 매핑됩니다.
 */
@Data                          // Lombok 어노테이션: getter, setter, toString, equals, hashCode 메서드를 자동 생성
@Entity                        // 이 클래스가 JPA 엔티티임을 선언
@NoArgsConstructor             // 기본 생성자를 자동 생성
@AllArgsConstructor            // 모든 필드를 인자로 받는 생성자를 자동 생성
@Table(name = "site_member")    // 데이터베이스의 "site_member" 테이블과 매핑
public class LoginEntity {

	/**
	 * 사용자 ID.
	 * @Id: 기본 키(primary key)임을 나타냄.
	 */
	@Id
	private Long userId;

	/**
	 * 사용자 이메일.
	 * @Column: 데이터베이스 컬럼 설정 - 유일(unique)하며, null 값을 허용하지 않습니다.
	 */
	@Column(unique = true, nullable = false)
	private Long studentNumber;

	/**
	 * 사용자 비밀번호.
	 * @Column: 데이터베이스 컬럼 설정 - null 값을 허용하지 않습니다.
	 */
	@Column(nullable = false)
	private String password;

	@Column(unique = true)
	private String email;

	/**
	 * 사용자의 권한 목록.
	 * 이 필드는 List<Permission> 형태로 엔티티에서 사용되며,
	 * 데이터베이스에는 문자열(JSON 배열 형식)로 저장됩니다.
	 *
	 * @Convert: PermissionConverter를 사용하여 List<Permission>과 String 간 변환을 처리합니다.
	 */
	@Column(name = "permissions")
	@Convert(converter = PermissionConverter.class)
	private List<Permission> permissions;

	/**
	 * 이메일과 비밀번호만으로 LoginEntity 객체를 생성하는 생성자.
	 * (주로 회원가입 시 기본 정보를 설정할 때 사용)
	 *
	 * @param email 사용자 이메일
	 * @param password 사용자 비밀번호
	 */

	private Long generation;

	public LoginEntity(String email, String password) {
		this.email = email;
		this.password = password;
	}
}