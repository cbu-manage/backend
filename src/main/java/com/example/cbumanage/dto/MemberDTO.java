package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MemberDTO {
	private Long id;
	private List<Role> role;
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
