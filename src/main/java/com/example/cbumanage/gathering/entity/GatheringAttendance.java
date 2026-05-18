package com.example.cbumanage.gathering.entity;

import com.example.cbumanage.gathering.entity.enums.AttendanceStatus;
import com.example.cbumanage.member.entity.CbuMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "gathering_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"gathering_id", "cbu_member_id"}))
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
    @JoinColumn(name = "cbu_member_id", nullable = false)
    private CbuMember member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static GatheringAttendance create(Gathering gathering, CbuMember member, AttendanceStatus status) {
        GatheringAttendance attendance = new GatheringAttendance();
        attendance.gathering = gathering;
        attendance.member = member;
        attendance.status = status;
        return attendance;
    }

    public void updateStatus(AttendanceStatus status) {
        this.status = status;
    }
}
