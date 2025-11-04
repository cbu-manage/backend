package com.example.cbumanage.authentication.entity.converter;

import com.example.cbumanage.authentication.authorization.Permission;
import jakarta.persistence.AttributeConverter;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * PermissionConverter는 JPA에서 엔티티의 List<Permission> 타입을 데이터베이스의 문자열 타입으로 변환하고,
 * 반대로 데이터베이스 문자열을 List<Permission> 타입으로 변환하기 위한 AttributeConverter 구현체입니다.
 *
 * 변환 시 JSON 배열 형식을 사용하여 데이터베이스에 저장합니다.
 */
public class PermissionConverter implements AttributeConverter<List<Permission>, String> {

	/**
	 * 엔티티에 저장된 List<Permission>을 데이터베이스 컬럼에 저장할 문자열로 변환합니다.
	 *
	 * @param attribute 변환할 Permission 리스트 (엔티티 속성)
	 * @return JSON 배열 형식의 문자열로 변환된 권한 목록
	 */
	@Override
	public String convertToDatabaseColumn(List<Permission> attribute) {
		// JSON 배열 객체 생성
		JSONArray jsonArray = new JSONArray();
		// 각 Permission 객체의 name 값을 JSON 배열에 추가
		for (Permission permission : attribute) {
			jsonArray.put(permission.getName());
		}
		// JSON 배열을 문자열로 반환 (예: '["member", "admin"]')
		return jsonArray.toString();
	}

	/**
	 * 데이터베이스에서 읽어온 문자열을 엔티티의 List<Permission>으로 변환합니다.
	 *
	 * @param dbData 데이터베이스에 저장된 JSON 배열 형식의 문자열
	 * @return 변환된 Permission 객체의 리스트. 만약 dbData가 null이면 빈 리스트 반환
	 */
	@Override
	public List<Permission> convertToEntityAttribute(String dbData) {
		// 만약 데이터가 null이면, 빈 리스트를 반환합니다.
		if (dbData == null) return List.of();

		// 변환된 Permission 객체들을 담을 리스트 생성
		List<Permission> list = new ArrayList<>();
		// dbData 문자열을 JSON 배열 객체로 변환
		JSONArray jsonArray = new JSONArray(dbData);
		// JSON 배열의 각 요소를 읽어 Permission 객체로 변환하여 리스트에 추가
		for (int i = 0; i < jsonArray.length(); i++) {
			// JSON 배열의 각 문자열 값을 Permission.getValue() 메서드를 통해 Permission 객체로 변환
			list.add(Permission.getValue(jsonArray.getString(i)));
		}
		return list;
	}
}
