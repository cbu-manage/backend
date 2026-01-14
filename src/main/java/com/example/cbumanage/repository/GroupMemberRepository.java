package com.example.cbumanage.repository;

import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByCbuMemberCbuMemberId(Long cbuMemberId);


}
