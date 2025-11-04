package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@Data
public class MemberCreateDTO {
	@NotNull
	private List<Role> role;
	@NotNull
	@NotEmpty
	private String name;
	@NotNull
	@NotEmpty
	private String phoneNumber;
	@NotNull
	@NotEmpty
	private String major;
	@NotNull
	@NotEmpty
	private String grade;
	@NotNull
	private Long studentNumber;
	@NotNull
	private Long generation;
	@NotNull
	private String note;
}
