package com.example.cbumanage.authentication.dto;

import com.example.cbumanage.authentication.authorization.Permission;
import com.example.cbumanage.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AccessToken {
	private Long userId;
	private Long studentNumber;
	private List<Role> role;
	private List<Permission> permission;
}
