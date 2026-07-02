package com.example.cbumanage.user.repository;

import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentNumber(Long studentNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserUuid(UUID userUuid);
    List<User> findAllByMemberStatus(MemberStatus memberStatus);
    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);
    Optional<User> findByStudentNumberAndDeletedAtIsNull(Long studentNumber);
    Optional<User> findByUserUuidAndDeletedAtIsNull(UUID userUuid);
    Page<User> findByDeletedAtIsNull(Pageable pageable);
    long countByMemberStatus(MemberStatus memberStatus);

    // 투표 운영진 수 (모집 시작 시점의 운영진 수 스냅샷용)
    long countByRoleInAndDeletedAtIsNull(Collection<Role> roles);

    // 투표 자격 운영진 목록 (투표현황에서 미투표자까지 표시하기 위함)
    List<User> findByRoleInAndDeletedAtIsNull(Collection<Role> roles);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.deletedAt IS NULL
              AND u.userId NOT IN (SELECT d.userId FROM Dues d WHERE d.term = :term)
            """)
    List<User> findAllWithoutDues(@Param("term") String term);

    //
    @Modifying
    @Query("UPDATE User u SET u.memberStatus = :to WHERE u.deletedAt IS NULL AND u.memberStatus = :from")
    int bulkUpdateMemberStatus(@Param("from") MemberStatus from, @Param("to") MemberStatus to);
}
