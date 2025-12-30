package com.example.cbumanage.utils;

import com.example.cbumanage.dto.MemberCreateDTO;
import com.example.cbumanage.dto.MemberDTO;
import com.example.cbumanage.dto.MemberUpdateDTO;
import com.example.cbumanage.exception.InvalidMapperSetupException;
import com.example.cbumanage.model.CbuMember;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

@Component
public class CbuMemberMapper {

	public CbuMemberMapper() {
		// CbuMember 필드 수가 변경되면 매퍼 로직 검토 필요
		if (CbuMember.class.getDeclaredFields().length != 12) {
			throw new InvalidMapperSetupException("CbuMember field is changed. Edit mapper and change the size value(" + CbuMember.class.getDeclaredFields().length + ")");
		}
	}

	public void map(MemberUpdateDTO memberUpdateDTO, CbuMember cbuMember) {
		for (Field field : MemberUpdateDTO.class.getDeclaredFields()) {
			if (field.getName().equals("cbuMemberId")) continue;
			StringBuilder method = new StringBuilder("");
			Object value;
			try {
				field.setAccessible(true);
				value = field.get(memberUpdateDTO);
				if (value == null) continue;
			} catch (IllegalAccessException e) {
				throw new InvalidMapperSetupException("Field: " + field.getName() + ", error: " + e.getMessage());
			}

			try {
				method = new StringBuilder(field.getName());
				method.setCharAt(0, Character.toUpperCase(method.charAt(0)));
				method.insert(0, "set");
				CbuMember.class.getMethod(method.toString(), field.getType()).invoke(cbuMember, value);
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new InvalidMapperSetupException("Field: " + field.getName() + ", method: " + method.toString() + ", " + String.join(", ", Arrays.asList(e.getStackTrace()).stream().map(stackTraceElement -> stackTraceElement.toString()).toList()));
			}
		}
	}

	public CbuMember map(MemberCreateDTO memberCreateDTO) {
		CbuMember cbuMember = new CbuMember();

		cbuMember.setRole(memberCreateDTO.getRole());
		cbuMember.setName(memberCreateDTO.getName());
		cbuMember.setPhoneNumber(memberCreateDTO.getPhoneNumber());
		cbuMember.setMajor(memberCreateDTO.getMajor());
		cbuMember.setGrade(memberCreateDTO.getGrade());
		cbuMember.setStudentNumber(memberCreateDTO.getStudentNumber());
		cbuMember.setGeneration(memberCreateDTO.getGeneration());
		cbuMember.setNote(memberCreateDTO.getNote());
		cbuMember.setEmail(null);
		return cbuMember;
	}
	public MemberDTO map(CbuMember cbuMember) {
		MemberDTO memberDTO = new MemberDTO(cbuMember.getCbuMemberId(), cbuMember.getRole(), cbuMember.getName(), cbuMember.getPhoneNumber(), cbuMember.getMajor(), cbuMember.getGrade(), cbuMember.getStudentNumber(), cbuMember.getGeneration(), cbuMember.getNote(), cbuMember.getDue(), cbuMember.getEmail());
		return memberDTO;
	}
	public List<MemberDTO> map(List<CbuMember> cbuMember) {
		return cbuMember.stream().map(this::map).toList();
	}
}
