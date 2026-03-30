package com.example.cbumanage.auth.dto;

import com.example.cbumanage.auth.authorization.Permission;
import com.example.cbumanage.member.entity.enums.Role;
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
