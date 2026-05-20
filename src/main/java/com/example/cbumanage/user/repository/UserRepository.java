package com.example.cbumanage.user.repository;

import com.example.cbumanage.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(Long studentNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserUuid(UUID userUuid);
    Optional<User> findByUserIdAndIsDeletedFalse(Long userId);
    Optional<User> findByStudentNumberAndIsDeletedFalse(Long studentNumber);
    Optional<User> findByUserUuidAndIsDeletedFalse(UUID userUuid);
    Page<User> findByIsDeletedFalse(Pageable pageable);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.isDeleted = false
              AND u.userId NOT IN (SELECT d.userId FROM Dues d WHERE d.term = :term)
            """)
    List<User> findAllWithoutDues(@Param("term") String term);
}
