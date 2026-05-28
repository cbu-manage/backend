package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.ApplicationNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 신청서 관련 알림(메일) 발송 이력 레포지토리 인터페이스.
 * 신청서 상태 변경 시 발송한 메일의 기록을 저장하고 조회하는 용도
 */
public interface ApplicationNotificationRepository extends JpaRepository<ApplicationNotification, Long> {

    // UUID로 단건 조회
    Optional<ApplicationNotification> findByNotificationUuid(String notificationUuid);

    // 신청서별 발송 이력 (최신순)
    List<ApplicationNotification> findByMemberApplicationIdOrderByCreatedAtDesc(Long memberApplicationId);
}