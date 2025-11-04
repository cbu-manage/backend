package com.example.cbumanage.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum Role {
	MEMBER(1), ADMIN(2);

	public final int value;
	public final String name;
	private static Map<String, Role> roleMap = initMap();

	Role(int value) {
		this.value = value;
		this.name = this.name();
	}

	@Override
	public String toString() {
		return this.name();
	}

	private static Map<String, Role> initMap() {
		HashMap<String, Role> result = new HashMap<>();
		for (Role value : Role.values()) {
			result.put(value.name, value);
		}
		return result;
	}

	public static Role getValue(String name) {
		return Role.roleMap.get(name);
	}
}
