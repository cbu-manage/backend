package com.example.cbumanage.model.converter;

import com.example.cbumanage.model.enums.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemberRoleConverterTest {
	@Test
	void memberRoleConverter() {
		MemberRoleConverter converter = new MemberRoleConverter();

		assertEquals(converter.convertToDatabaseColumn(List.of(Role.MEMBER)), 1);

		List<Role> roles = converter.convertToEntityAttribute(1L);
		assertEquals(roles.size(), 1);
		assertEquals(roles.get(0), Role.MEMBER);
	}

}