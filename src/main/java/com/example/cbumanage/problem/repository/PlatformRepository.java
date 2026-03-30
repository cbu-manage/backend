package com.example.cbumanage.problem.repository;

import com.example.cbumanage.problem.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PlatformRepository extends JpaRepository<Platform, Integer> {
}
