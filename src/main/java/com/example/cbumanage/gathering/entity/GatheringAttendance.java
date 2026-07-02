package com.example.cbumanage.gathering.entity;

import com.example.cbumanage.gathering.entity.enums.AttendanceStatus;
import com.example.cbumanage.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "gathering_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"gathering_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 사용자가 명시적으로 투표한 시각. 자동 초기화(NOT_RESPONDED)된 레코드는 null
    private LocalDateTime votedAt;

    // 자동 초기화용 (allMembersTarget=true 시 NOT_RESPONDED로 일괄 생성)
    public static GatheringAttendance create(Gathering gathering, User member, AttendanceStatus status) {
        GatheringAttendance attendance = new GatheringAttendance();
        attendance.gathering = gathering;
        attendance.member = member;
        attendance.status = status;
        return attendance;
    }

    // 사용자 명시적 투표용 (신규 레코드 생성 시 votedAt 세팅)
    public static GatheringAttendance createWithVote(Gathering gathering, User member, AttendanceStatus status) {
        GatheringAttendance attendance = create(gathering, member, status);
        attendance.votedAt = LocalDateTime.now();
        return attendance;
    }

    public void updateStatus(AttendanceStatus status) {
        this.status = status;
        this.votedAt = LocalDateTime.now();
    }
}
