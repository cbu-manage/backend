package com.example.cbumanage.repository;

import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.model.enums.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findById(long id);
    List<Group> findByGroupNameContaining(String groupName);


    //특정 Status인 멤버만 카운트하는 메소드입니다
    @Query("""
    select count(m)
    from GroupMember m
    where m.group.id = :groupId
    and m.groupMemberStatus = :groupMemberStatus
""")
    int countByGroupIdAndStatus(long groupId, GroupMemberStatus groupMemberStatus);

    /*
    그룹 멤버의 id와 현재  통해 현재 멤버가 가입되어 있는 그룹의 리스트를 뽑아냅니다
     */
    @Query("""
    select m.group
    from GroupMember m
    where m.cbuMember.cbuMemberId =:userId
""")
    List<Group> findByUserId(Long userId);


}
