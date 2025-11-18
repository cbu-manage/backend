package com.example.cbumanage.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentNumberAndPasswordDTO {
	private Long studentNumber;
	private String password;
}
