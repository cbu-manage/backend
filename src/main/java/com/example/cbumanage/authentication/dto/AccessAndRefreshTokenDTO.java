package com.example.cbumanage.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessAndRefreshTokenDTO {
	private String accessToken;
	private String refreshToken;
}
