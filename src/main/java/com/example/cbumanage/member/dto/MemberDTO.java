package com.example.cbumanage.member.dto;

import com.example.cbumanage.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDTO {
	private Long id;
	private Role role;
	private String name;
	private String phoneNumber;
	private String major;
	private String grade;
	private Long studentNumber;
	private Long generation;
	private String note;
	private Boolean due;
	private String email;
}