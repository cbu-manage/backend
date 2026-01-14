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
@Table(name="cbu_groups")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name",unique = true, nullable = false)
    private String groupName;

    //그룹의 최소활동인원과 최대 활동 인원을 표기합니다
    public int minActiveMembers;

    public int maxActiveMembers;

    @CreatedDate
    public LocalDateTime createdAt;

    @LastModifiedDate
    public LocalDateTime updatedAt;

    //그룹의 모집상태를 표기하는 enum입니다 모집중/모집종료로 구분됩니다
    @Enumerated(EnumType.STRING)
    public GroupRecruitmentStatus recruitmentStatus;

    //그룹의 상태를 표기하는 enum입니다. 활동/비활동으로 구분됩니다
    @Enumerated(EnumType.STRING)
    public GroupStatus status ;

    //그룹의 생성자, 상태들은 기본적으로 모집 안함, 비활성 상태로 시작
    public Group(String groupName, int minActiveMembers, int maxActiveMembers) {

        this.groupName = groupName;
        this.minActiveMembers = minActiveMembers;
        this.maxActiveMembers = maxActiveMembers;
        this.recruitmentStatus = GroupRecruitmentStatus.CLOSED;
        this.status = GroupStatus.INACTIVE;
    }

    public static Group create(String groupName,int minActiveMembers,int maxActiveMembers) {
        return new Group(groupName,minActiveMembers,maxActiveMembers);
    }

    public void changeGroupName(String groupName) {
        this.groupName = groupName;
    }
    public void changeMinActiveMembers(int minActiveMembers) {this.minActiveMembers = minActiveMembers;}
    public void changeMaxActiveMembers(int maxActiveMembers) {this.maxActiveMembers = maxActiveMembers;}

    @OneToMany(mappedBy = "group" ,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<GroupMember> members = new ArrayList<>();

    public void addMember(GroupMember member) {
       this.members.add(member);
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
