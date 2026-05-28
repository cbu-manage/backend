package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.ApplicationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 지원서 답변 레포지토리 인터페이스.
 */
public interface ApplicationAnswerRepository extends JpaRepository<ApplicationAnswer, Long> {

    /**
     * 신청서의 모든 답변 조회(운영진이 신청서를 볼 때).
     * 화면에서는 question_snapshot으로 질문이 같이 보여야 함
     */
    List<ApplicationAnswer> findByApplicationId(Long applicationId);

    // 신청서별 답변 일괄 삭제 (신청서 취소 또는 재제출 시)
    void deleteByApplicationId(Long applicationId);
}