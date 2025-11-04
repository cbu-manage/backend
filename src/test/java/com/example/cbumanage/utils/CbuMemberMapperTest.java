package com.example.cbumanage.utils;

import com.example.cbumanage.dto.MemberUpdateDTO;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CbuMemberMapperTest {
	CbuMemberMapper cbuMemberMapper = new CbuMemberMapper();
	@Test
	void mapperTest() {
		CbuMember cbuMember = new CbuMember(1L, List.of(), "name", "123-456-789", "Software", "2", 2023158029L, 20L, "", true, null);
		MemberUpdateDTO memberUpdateDTO;

		memberUpdateDTO = new MemberUpdateDTO(1L, null, null, null, null, null, null, null, null, null, null);
		cbuMemberMapper.map(memberUpdateDTO, cbuMember);
		assertEquals(cbuMember.getCbuMemberId(), memberUpdateDTO.getCbuMemberId());
		assertNotNull(cbuMember.getName());

		memberUpdateDTO = new MemberUpdateDTO(1L, List.of(Role.MEMBER), "null", null, null, null, null, null, null, null, null);
		cbuMemberMapper.map(memberUpdateDTO, cbuMember);
		assertEquals(cbuMember.getRole(), memberUpdateDTO.getRole());
		assertEquals(cbuMember.getName(), memberUpdateDTO.getName());
	}
}
