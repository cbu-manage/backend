package com.example.cbumanage.group.entity;

import com.example.cbumanage.global.error.CustomException;
import com.example.cbumanage.group.entity.enums.GroupRecruitmentStatus;
import com.example.cbumanage.group.entity.enums.GroupStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.example.cbumanage.global.error.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="cbu_groups")
@Comment("CBU 동아리/모임 그룹 정보 테이블")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@BatchSize(size = 100)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("그룹 고유 식별자")
    private Long id;

    @Column(name = "group_name", nullable = false)
    @Comment("그룹명")
    private String groupName;

    @Comment("최소 활동 인원 설정값")
    private int minActiveMembers;

    @Comment("최대 활동 가능 인원 설정값")
    private Integer maxActiveMembers;

    @Column(name = "post_id")
    @Comment("연결된 게시글 ID (프로젝트/스터디 모집글). 없으면 null")
    private Long postId;

    @Column(name = "category")
    @Comment("연결된 게시글 카테고리 번호 (스터디=1, 프로젝트=2)")
    private Integer category;

    @CreatedDate
    @Column(updatable = false)
    @Comment("그룹 생성 일시")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Comment("그룹 정보 수정 일시")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Comment("모집 상태 (OPEN: 모집중, CLOSED: 모집종료)")
    private GroupRecruitmentStatus recruitmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Comment("그룹 승인 상태 (ACCEPTED: 승인, REJECTED: 반려, PENDING: 승인 대기중, INACTIVE:활동종료")
    private GroupStatus status ;

    @Comment("반려 사유")
    private String rejectReason ;

    @Comment("그룹 삭제 여부(Soft Delete")
    private Boolean isDeleted = false;

    //그룹의 생성자, 상태들은 기본적으로 모집 안함, 승인 대기중 상태로 시작
    public Group(String groupName, int minActiveMembers, Integer maxActiveMembers, Long postId, int category)  {
        if (maxActiveMembers < 2) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "최대 모집 인원은 본인을 포함해 최소 2명 이상이어야 합니다.");
        }
        this.groupName = groupName;
        this.minActiveMembers = minActiveMembers;
        this.maxActiveMembers = maxActiveMembers;
        this.postId = postId;
        this.category = category;
        this.recruitmentStatus = GroupRecruitmentStatus.CLOSED;
        this.status = GroupStatus.PENDING;
    }

    public static Group create(String groupName, int minActiveMembers, Integer maxActiveMembers, Long postId, int category) {
        return new Group(groupName, minActiveMembers, maxActiveMembers, postId, category);
    }

    public void changeGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void changeMaxActiveMembers(int newMaxMember) {this.maxActiveMembers = newMaxMember;}

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "group" ,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<GroupMember> members = new ArrayList<>();

    public void addMember(GroupMember member) {
       this.members.add(member);
    }

    /*
    그룹의 승인 상태를 바꾸는 메소드 입니다
     */
    public void approve() {
        this.status = GroupStatus.ACTIVE;
        this.rejectReason = null; // 승인 시 반려 사유 초기화
    }

    public void reject(String rejectReason) {
        this.status = GroupStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    public void resubmit() {
        this.status = GroupStatus.RESUBMITTED;
    }

    /*
    그룹의 모집 상태를 변경시키는 메소드 입니다
     */

    public void openRecruitment() {
        this.recruitmentStatus = GroupRecruitmentStatus.OPEN;
    }

    public void closeRecruitment() {
        this.recruitmentStatus = GroupRecruitmentStatus.CLOSED;
    }

    public void delete() {
        this.isDeleted = true;
    }


}
