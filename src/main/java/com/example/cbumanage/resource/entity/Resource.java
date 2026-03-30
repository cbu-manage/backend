package com.example.cbumanage.resource.entity;

import com.example.cbumanage.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 자료방 게시글 정보를 저장하는 엔티티.
 * 제목과 외부 링크로 구성됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resource")
@SQLDelete(sql = "UPDATE resource SET deleted_at = CURRENT_TIMESTAMP WHERE resource_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * 자료 외부 링크
     */
    @Column(nullable = false, length = 500)
    private String link;

    /**
     * 소프트 딜리트 시간 (null이면 정상, 값이 있으면 삭제된 게시글)
     */
    private LocalDateTime deletedAt;

    /*
    OG (Open Graph) 메타데이터 필드입니다.
    OG는 링크 공유 시 미리보기로 보여지는 제목, 설명, 이미지 등을 정의합니다.
     */

    // Open Graph 대표 이미지 URL
    @Column(columnDefinition = "TEXT")
    private String ogImage;

    // Open Graph 설명
    @Column(columnDefinition = "TEXT")
    private String ogDescription;

    // Open Graph 마지막 갱신 시간
    private LocalDateTime ogUpdatedAt;

    @Builder
    public Resource(Post post, String link) {
        this.post = post;
        this.link = link;
    }

    public void updateOg(String ogImage, String ogDescription) {
        this.ogImage = ogImage;
        this.ogDescription = ogDescription;
        this.ogUpdatedAt = LocalDateTime.now();
    }

}
