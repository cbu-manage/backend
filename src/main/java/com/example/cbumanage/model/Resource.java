package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 자료방 게시글 정보를 저장하는 엔티티.
 * 제목과 외부 링크로 구성됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "resource")
@SQLDelete(sql = "UPDATE resource SET deleted_at = CURRENT_TIMESTAMP WHERE resource_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    /**
     * 게시글 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private CbuMember member;

    /**
     * 자료 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 자료 외부 링크
     */
    @Column(nullable = false, length = 500)
    private String link;

    /**
     * 작성 시간
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 소프트 딜리트 시간 (null이면 정상, 값이 있으면 삭제된 게시글)
     */
    private LocalDateTime deletedAt;

    @Builder
    public Resource(CbuMember member, String title, String link) {
        this.member = member;
        this.title = title;
        this.link = link;
    }
}
