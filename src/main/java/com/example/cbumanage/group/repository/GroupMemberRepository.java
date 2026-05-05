package com.example.cbumanage.group.repository;

import com.example.cbumanage.group.entity.GroupMember;
import com.example.cbumanage.group.entity.enums.GroupMemberRole;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    boolean existsByUserUserIdAndGroupId(Long userId, Long groupId);
    List<GroupMember> findByGroupIdAndGroupMemberStatus(Long groupId, GroupMemberStatus status);

    GroupMember findByGroupIdAndUserUserId(Long groupId, Long userId);

    @Query("""
    select gm
    from GroupMember gm
    where gm.user.userId = :userId
      and gm.groupMemberRole <> :LEADER
      and gm.group.isDeleted = false
      and (:category is null or gm.group.category = :category)
    """)
    Page<GroupMember> findMyApplicationsByCategory(Long userId, Integer category, GroupMemberRole LEADER, Pageable pageable);

    @Query("""
    SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END
    FROM GroupMember gm
    WHERE gm.user.userId = :userId
      AND gm.group.id = :groupId
      AND gm.groupMemberStatus = :status
""")
    boolean existsActiveMember(Long userId, Long groupId, GroupMemberStatus status);
}
