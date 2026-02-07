package com.example.cbumanage.repository;

import com.example.cbumanage.model.StudyApply;
import com.example.cbumanage.model.enums.StudyApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyApplyRepository extends JpaRepository<StudyApply, Long> {

    // 특정 스터디의 모든 신청 조회
    List<StudyApply> findByStudyId(Long studyId);

    // 특정 스터디의 상태별 신청 조회
    List<StudyApply> findByStudyIdAndStatus(Long studyId, StudyApplyStatus status);

    // 특정 스터디에 특정 사용자가 신청했는지 확인
    boolean existsByStudyIdAndApplicantCbuMemberId(Long studyId, Long applicantId);

    // 특정 스터디에서 특정 신청 조회
    Optional<StudyApply> findByIdAndStudyId(Long applyId, Long studyId);

    // 특정 사용자가 수락된 스터디 신청 목록
    List<StudyApply> findByApplicantCbuMemberIdAndStatus(Long applicantId, StudyApplyStatus status);
}
