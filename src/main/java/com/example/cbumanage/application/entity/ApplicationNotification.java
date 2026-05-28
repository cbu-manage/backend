package com.example.cbumanage.application.entity;

import com.example.cbumanage.application.entity.enums.MailNotiType;
import com.example.cbumanage.application.entity.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application_notification",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_uuid", columnNames = "notification_uuid")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "notification_uuid", nullable = false, unique = true, length = 36)
    private String notificationUuid;

    @Column(name = "member_application_id", nullable = false)
    private Long memberApplicationId;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 50)
    private MailNotiType templateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private ApplicationNotification(Long memberApplicationId, String email,
                                    MailNotiType templateType,
                                    NotificationStatus status, String errorMessage) {
        this.notificationUuid = UUID.randomUUID().toString();
        this.memberApplicationId = memberApplicationId;
        this.email = email;
        this.templateType = templateType;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    // sent와 failed에 대한 정적 팩토리 메서드. 같은 클래스를 여러 다른 의도로 만들 때 사용한다.

    public static ApplicationNotification sent(Long applicationId, String email,
                                               MailNotiType template) {
        return ApplicationNotification.builder()
                .memberApplicationId(applicationId)
                .email(email)
                .templateType(template)
                .status(NotificationStatus.SENT)
                .build();
    }

    public static ApplicationNotification failed(Long applicationId, String email,
                                                 MailNotiType template, String error) {
        return ApplicationNotification.builder()
                .memberApplicationId(applicationId)
                .email(email)
                .templateType(template)
                .status(NotificationStatus.FAILED)
                .errorMessage(error)
                .build();
    }
}