package com.example.cbumanage.repository;

import com.example.cbumanage.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PlatformRepository extends JpaRepository<Platform, Integer> {
}
