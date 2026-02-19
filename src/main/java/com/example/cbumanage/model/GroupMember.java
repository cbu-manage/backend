package com.example.cbumanage.model;


import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "group_member")
@Comment("그룹별 소속 회원 정보 및 권한 관리 테이블")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("그룹 멤버 관계 고유 식별자")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="group_id",nullable = false)
    @Comment("소속된 그룹 ID (FK)")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cbumember_id",nullable = false)
    @Comment("그룹에 소속된 회원 ID (FK)")
    private CbuMember cbuMember;

    @CreatedDate
    @Column(updatable = false)
    @Comment("그룹 가입/신청 일시")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Comment("멤버 정보 수정 일시")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Comment("그룹 내 멤버 상태 (PENDING: 가입 대기, ACTIVE: 활동, INACTIVE: 비활동, REJECTED: 가입거절)")
    private GroupMemberStatus groupMemberStatus;

    @Enumerated(EnumType.STRING)
    @Comment("그룹 내 멤버 권한 (LEADER, MEMBER)")
    private GroupMemberRole groupMemberRole;

    public GroupMember(Group group,
                       CbuMember cbuMember,
                       GroupMemberStatus groupMemberStatus,
                       GroupMemberRole groupMemberRole) {
        this.group = group;
        this.cbuMember = cbuMember;
        this.groupMemberStatus = groupMemberStatus;
        this.groupMemberRole = groupMemberRole;
    }

    public static GroupMember create(Group group,CbuMember cbuMember, GroupMemberStatus groupMemberStatus, GroupMemberRole groupMemberRole) {
        return  new GroupMember(group,cbuMember, groupMemberStatus, groupMemberRole);
    }


    public void changeStatus(GroupMemberStatus groupMemberStatus) {
        this.groupMemberStatus = groupMemberStatus;
    }

    public void changeRole(GroupMemberRole groupMemberRole) {
        this.groupMemberRole = groupMemberRole;
    }

}
