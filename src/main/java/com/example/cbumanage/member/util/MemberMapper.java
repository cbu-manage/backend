package com.example.cbumanage.member.util;

import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberMapper {

	public User map(MemberCreateDTO memberCreateDTO, String encodedDefaultPassword) {
		User user = new User(
				null,
				memberCreateDTO.getStudentNumber(),
				encodedDefaultPassword,
				null,
				memberCreateDTO.getName(),
				memberCreateDTO.getPhoneNumber(),
				memberCreateDTO.getMajor(),
				memberCreateDTO.getGrade(),
				memberCreateDTO.getGeneration()
		);
		user.changeRole(memberCreateDTO.getRole() != null ? memberCreateDTO.getRole() : Role.ROLE_USER);
		user.updateMemberInfo(
				memberCreateDTO.getName(),
				memberCreateDTO.getPhoneNumber(),
				memberCreateDTO.getMajor(),
				memberCreateDTO.getGrade(),
				memberCreateDTO.getStudentNumber(),
				memberCreateDTO.getGeneration(),
				memberCreateDTO.getNote()
		);
		return user;
	}

	public void map(MemberUpdateDTO memberUpdateDTO, User user) {
		if (memberUpdateDTO.getRole() != null) {
			user.changeRole(memberUpdateDTO.getRole());
		}
		user.updateMemberInfo(
				memberUpdateDTO.getName() != null ? memberUpdateDTO.getName() : user.getName(),
				memberUpdateDTO.getPhoneNumber() != null ? memberUpdateDTO.getPhoneNumber() : user.getPhoneNumber(),
				memberUpdateDTO.getMajor() != null ? memberUpdateDTO.getMajor() : user.getMajor(),
				memberUpdateDTO.getGrade() != null ? memberUpdateDTO.getGrade() : user.getGrade(),
				memberUpdateDTO.getStudentNumber() != null ? memberUpdateDTO.getStudentNumber() : user.getStudentNumber(),
				memberUpdateDTO.getGeneration() != null ? memberUpdateDTO.getGeneration() : user.getGeneration(),
				memberUpdateDTO.getNote() != null ? memberUpdateDTO.getNote() : user.getNote()
		);
	}

	public MemberDTO map(User user) {
		return new MemberDTO(
				user.getUserId(),
				user.getRole(),
				user.getName(),
				user.getPhoneNumber(),
				user.getMajor(),
				user.getGrade(),
				user.getStudentNumber(),
				user.getGeneration(),
				user.getNote(),
				user.getMemberStatus() == MemberStatus.ACTIVE,
				user.getEmail()
		);
	}

	public List<MemberDTO> map(List<User> users) {
		return users.stream().map(this::map).toList();
	}
}