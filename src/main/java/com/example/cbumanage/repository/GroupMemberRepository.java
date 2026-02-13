package com.example.cbumanage.repository;

import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.GroupMember;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByCbuMemberCbuMemberId(Long cbuMemberId);
    boolean existsByCbuMemberCbuMemberIdAndGroupId(Long cbuMemberId, Long groupId);
    List<GroupMember> findByGroupIdAndGroupMemberStatus(Long groupId, GroupMemberStatus status);

    GroupMember findByGroupIdAndCbuMemberCbuMemberId(Long groupId, Long cbuMemberId);

    @Query("""
    SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END
    FROM GroupMember gm
    WHERE gm.cbuMember.cbuMemberId = :cbuMemberId
      AND gm.group.id = :groupId
      AND gm.groupMemberStatus = :status
""")
    boolean existsActiveMember(Long cbuMemberId, Long groupId, GroupMemberStatus status);
}
