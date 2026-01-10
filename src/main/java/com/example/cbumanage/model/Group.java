package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.model.enums.GroupStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="groups")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name",unique = true, nullable = false)
    private String groupName;

    public int minActiveMembers;

    public int maxActiveMembers;

    @CreatedDate
    public LocalDateTime createdAt;

    @LastModifiedDate
    public LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    public GroupRecruitmentStatus recruitmentStatus;

    @Enumerated(EnumType.STRING)
    public GroupStatus status;

    public Group(String groupName, int minActiveMembers, int maxActiveMembers) {

        this.groupName = groupName;
        this.minActiveMembers = minActiveMembers;
        this.maxActiveMembers = maxActiveMembers;
    }

    public static Group create(String groupName,CbuMember cbuMember,int minActiveMembers,int maxActiveMembers) {
        Group  group = new Group(groupName,minActiveMembers,maxActiveMembers);
        GroupMember leader = GroupMember.create(group,cbuMember,GroupMemberStatus.ACTIVE,GroupMemberRole.LEADER);
        group.members.add(leader);
        return group;
    }

    public void changeGroupName(String groupName) {
        this.groupName = groupName;
    }


    @OneToMany(mappedBy = "group" ,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<GroupMember> members = new ArrayList<>();

    public void addMember(CbuMember cbuMember) {
       this.members.add(GroupMember.create(this,cbuMember,GroupMemberStatus.PENDING,GroupMemberRole.MEMBER));
    }

    /*
    그룹의 활동 상황을 바꾸는 메소드 입니다
     */
    public void activate() {
        if( this.status == GroupStatus.ACTIVE) return ;
        this.status = GroupStatus.ACTIVE;
    }

    public void deactivate() {
        if( this.status == GroupStatus.INACTIVE) return ;
        this.status = GroupStatus.INACTIVE;
    }

    /*
    그룹의 모집 상태를 변경시키는 메소드 입니다
     */

    public void openRecruitment() {
        if (this.recruitmentStatus == GroupRecruitmentStatus.OPEN) return;
        this.recruitmentStatus = GroupRecruitmentStatus.OPEN;
    }

    public void closeRecruitment() {
        if (this.recruitmentStatus == GroupRecruitmentStatus.CLOSED) return;
        this.recruitmentStatus = GroupRecruitmentStatus.CLOSED;
    }


}
