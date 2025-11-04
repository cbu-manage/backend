package com.example.cbumanage.model.converter;

import com.example.cbumanage.model.enums.Role;
import jakarta.persistence.AttributeConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MemberRoleConverter는 JPA AttributeConverter를 구현하여,
 * 엔티티의 List<Role> 타입을 데이터베이스에 저장할 때 하나의 long 값으로 변환하고,
 * 데이터베이스에 저장된 long 값을 다시 List<Role>로 복원하는 역할을 수행합니다.
 *
 * 이 변환기는 각 Role을 비트 플래그(bit flag)로 표현하여, 여러 역할을 단일 long 값에 저장할 수 있도록 합니다.
 */
public class MemberRoleConverter implements AttributeConverter<List<Role>, Long> {

	/**
	 * 엔티티 속성인 List<Role>을 데이터베이스 컬럼에 저장할 long 값으로 변환합니다.
	 *
	 * 각 Role 객체의 value 값을 이용하여 해당 역할에 해당하는 비트를 설정합니다.
	 * 예를 들어, Role의 value가 1이면 1L << (1-1) 즉, 1이 되고,
	 * value가 2이면 1L << (2-1) 즉, 2가 됩니다.
	 * 여러 역할이 있을 경우, 각 역할의 비트를 OR 연산(|)으로 결합하여 하나의 long 값으로 만듭니다.
	 *
	 * @param attribute 엔티티에서 전달된 Role 리스트
	 * @return 모든 역할의 비트가 결합된 long 값 (예: [ROLE1, ROLE2] -> 3)
	 */
	@Override
	public Long convertToDatabaseColumn(List<Role> attribute) {
		long result = 0L;  // 초기 결과값은 모든 비트가 0인 상태입니다.

		// 리스트에 있는 각 Role에 대해 반복합니다.
		for (Role role : attribute) {
			// 각 역할의 비트 마스크를 생성합니다.
			// role.value - 1: 역할의 순서를 0부터 시작하도록 조정.
			long v = 1L << (role.value - 1);
			// 기존 결과값과 새 비트 마스크를 OR 연산하여, 해당 역할의 비트를 설정합니다.
			result |= v;
		}

		return result;  // 최종적으로 설정된 모든 역할의 비트가 결합된 long 값을 반환합니다.
	}

	/**
	 * 데이터베이스에 저장된 long 값을 엔티티 속성인 List<Role>로 변환합니다.
	 *
	 * 데이터베이스에서 읽어온 long 값의 각 비트를 확인하여, 해당하는 Role 객체들을 리스트에 추가합니다.
	 * 각 Role의 비트 마스크(1L << (role.value - 1))와 데이터베이스 값의 AND 연산 결과가 0보다 크면,
	 * 해당 Role이 활성화되어 있다고 판단하여 리스트에 추가합니다.
	 *
	 * @param dbData 데이터베이스에서 읽어온 long 값 (비트 플래그로 표현된 역할들)
	 * @return 복원된 Role 객체들의 불변 리스트
	 */
	@Override
	public List<Role> convertToEntityAttribute(Long dbData) {
		List<Role> result = new ArrayList<>();

		// Role 열거형에 정의된 모든 역할에 대해 반복합니다.
		for (Role role : Role.values()) {
			// 각 역할에 해당하는 비트 마스크를 생성합니다.
			long v = 1L << (role.value - 1);
			// 데이터베이스 값과 비트 마스크를 AND 연산하여, 해당 역할의 비트가 설정되어 있는지 확인합니다.
			if ((dbData & v) > 0) {
				// 비트가 설정되어 있다면, 해당 Role 객체를 결과 리스트에 추가합니다.
				result.add(role);
			}
		}

		// 결과 리스트를 불변 리스트로 반환하여 수정되지 않도록 합니다.
		return Collections.unmodifiableList(result);
	}
}
