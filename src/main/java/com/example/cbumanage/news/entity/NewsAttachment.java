package com.example.cbumanage.news.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "news_attachment",
        indexes = @Index(name = "idx_news_attachment_news", columnList = "news_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "news_id", nullable = false)
    @Getter(AccessLevel.NONE)
    private News news;

    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private NewsAttachment(News news, String s3Key, String originalFileName, String contentType, long fileSize) {
        this.news = news;
        this.s3Key = s3Key;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public static NewsAttachment create(News news, String s3Key, String originalFileName, String contentType, long fileSize) {
        return new NewsAttachment(news, s3Key, originalFileName, contentType, fileSize);
    }

    public boolean belongsTo(Long newsId) {
        return news.getNewsId().equals(newsId);
    }
}
