package com.example.cbumanage.auth.dto;

import com.example.cbumanage.auth.entity.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessAndRefreshTokenObjectDTO {
	private AccessToken accessToken;
	private RefreshToken refreshToken;
	private String accessTokenAsString;
	private String refreshTokenAsString;
}
