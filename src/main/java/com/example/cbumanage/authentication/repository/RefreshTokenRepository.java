package com.example.cbumanage.authentication.repository;

import com.example.cbumanage.authentication.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
	public List<RefreshToken> findAllByExpLessThan(Long exp);
	public List<RefreshToken> findAllByUserId(Long userId);
}
