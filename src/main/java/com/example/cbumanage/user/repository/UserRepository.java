package com.example.cbumanage.user.repository;

import com.example.cbumanage.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(Long studentNumber);
    Optional<User> findByUserUuid(UUID userUuid);
}
