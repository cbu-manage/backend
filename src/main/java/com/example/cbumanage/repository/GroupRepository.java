package com.example.cbumanage.repository;

import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findById(long id);
    Group findByGroupName(String name);

    @Query("""
    select count(m)
    from GroupMember m
    where m.group.id = :groupId
    and m.groupMemberStatus = :status
""")
    int countByGroupIdAndStatus(long groupId, GroupMemberStatus status);




    

}
