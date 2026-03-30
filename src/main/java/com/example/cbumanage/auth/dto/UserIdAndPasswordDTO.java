package com.example.cbumanage.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserIdAndPasswordDTO {
	@NotNull
	private Long userId;
	@Size(min = 8)
	@NotNull
	private String password;
}
