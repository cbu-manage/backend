package com.example.cbumanage.group.entity;


import com.example.cbumanage.group.entity.enums.GroupMemberRole;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.user.entity.User;
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
    @JoinColumn(name = "user_id",nullable = false)
    @Comment("그룹에 소속된 회원 ID (FK)")
    private User user;

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

    @Comment("가입 거절 사유")
    private String memberRejectReason;

    @Column(nullable = false)
    @Comment("팀장이 이 회원의 신청을 거절한 횟수 (재신청 제한 기준). 모집 마감 일괄 거절은 세지 않는다")
    private int rejectedCount = 0;

    @Enumerated(EnumType.STRING)
    @Comment("그룹 내 멤버 권한 (LEADER, MEMBER)")
    private GroupMemberRole groupMemberRole;

    public GroupMember(Group group,
                       User user,
                       GroupMemberStatus groupMemberStatus,
                       GroupMemberRole groupMemberRole) {
        this.group = group;
        this.user = user;
        this.groupMemberStatus = groupMemberStatus;
        this.groupMemberRole = groupMemberRole;
    }

    public static GroupMember create(Group group, User user, GroupMemberStatus groupMemberStatus, GroupMemberRole groupMemberRole) {
        return  new GroupMember(group,user, groupMemberStatus, groupMemberRole);
    }


    public void active(){
        this.groupMemberStatus = GroupMemberStatus.ACTIVE;
        this.memberRejectReason = null;
    }
    /** 팀장이 직접 거절. 실수로 눌렀을 수 있으니 바로 막지 않고 횟수만 센다 */
    public void rejectByLeader(String reason) {
        reject(reason);
        this.rejectedCount++;
    }

    /** 모집 마감으로 대기자를 정리. 팀장의 거절 의사가 아니므로 횟수에 넣지 않는다 */
    public void rejectOnRecruitmentClose(String reason) {
        reject(reason);
    }

    private void reject(String reason) {
        this.groupMemberStatus = GroupMemberStatus.REJECTED;
        this.memberRejectReason = reason;
    }
    public void pending(){
        this.groupMemberStatus = GroupMemberStatus.PENDING;
    }

    public void changeRole(GroupMemberRole groupMemberRole) {
        this.groupMemberRole = groupMemberRole;
    }

}
