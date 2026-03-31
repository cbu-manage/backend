package com.example.cbumanage.group.repository;

import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.group.entity.enums.GroupRecruitmentStatus;
import com.example.cbumanage.group.entity.enums.GroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Group findById(long id);

    //soft delete 안된 그룹 전체 조회
    List<Group> findAllByIsDeletedFalse();

    //soft delete 안된 값만 조회
    Optional<Group> findByIdAndIsDeletedFalse(Long id);

    List<Group> findByGroupNameContaining(String groupName);

    //전체 그룹 상태 별로 조회
    @Query("""
    select g
    from Group g
    where g.isDeleted = false
      and (:groupStatus is null or g.status = :groupStatus)
      and g.recruitmentStatus = :recruitmentStatus
    """)
    Page<Group> findByGroupStatus(@Param("groupStatus") GroupStatus groupStatus,
                                  @Param("recruitmentStatus") GroupRecruitmentStatus recruitmentStatus,
                                  Pageable pageable);


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
    and m.groupMemberStatus =:memberStatus
    and m.group.isDeleted = false
""")
    List<Group> findByUserId(Long userId, GroupMemberStatus memberStatus);


}
