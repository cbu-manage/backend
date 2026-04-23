package com.example.cbumanage.reportmember.repository;

import com.example.cbumanage.reportmember.entity.ReportMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportMemberRepository extends JpaRepository<ReportMember, Long> {
    List<ReportMember> findByReportId(Long reportId);
    void deleteByReportId(Long reportId);
}
