package com.example.cbumanage.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;


public class UUIDProvider {
	public static UUID random() {
		// TODO : Make real random
		return UUID.randomUUID();
	}
}
