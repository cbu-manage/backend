package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.model.enums.GroupStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="cbu_groups")
@Comment("CBU 동아리/모임 그룹 정보 테이블")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("그룹 고유 식별자")
    private Long id;

    @Column(name = "group_name",unique = true, nullable = false)
    @Comment("그룹명 (중복 불가)")
    private String groupName;

    @Comment("최소 활동 인원 설정값")
    public int minActiveMembers;

    @Comment("최대 활동 가능 인원 설정값")
    public int maxActiveMembers;

    @CreatedDate
    @Column(updatable = false)
    @Comment("그룹 생성 일시")
    public LocalDateTime createdAt;

    @LastModifiedDate
    @Comment("그룹 정보 수정 일시")
    public LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Comment("모집 상태 (OPEN: 모집중, CLOSED: 모집종료)")
    public GroupRecruitmentStatus recruitmentStatus;

    @Enumerated(EnumType.STRING)
    @Comment("그룹 활성화 상태 (ACTIVE: 활동, INACTIVE: 비활동)")
    public GroupStatus status ;

    @Comment("그룹 삭제 여부(Soft Delete")
    public Boolean isDeleted = false;

    //그룹의 생성자, 상태들은 기본적으로 모집 안함, 비활성 상태로 시작
    public Group(String groupName, int minActiveMembers, int maxActiveMembers)  {

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

    public void delete() {
        this.isDeleted = true;
    }


}
