package com.example.cbumanage.reportmember.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_member_id")
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public static ReportMember create(Long reportId, Long memberId) {
        ReportMember rm = new ReportMember();
        rm.reportId = reportId;
        rm.memberId = memberId;
        return rm;
    }
}
