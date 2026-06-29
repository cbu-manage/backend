package com.example.cbumanage.news.entity;

import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import com.example.cbumanage.news.entity.enums.NewsletterType;
import com.example.cbumanage.post.entity.enums.PostCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "news",
        indexes = {
                @Index(name = "idx_news_category", columnList = "news_category"),
                @Index(name = "idx_news_pinned_pinned_at", columnList = "is_pinned, pinned_at"),
                @Index(name = "idx_news_deleted_at", columnList = "deleted_at"),
                @Index(name = "idx_news_newsletter_type", columnList = "newsletter_type")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE news SET deleted_at = CURRENT_TIMESTAMP WHERE news_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long newsId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    @Getter(AccessLevel.NONE)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "news_category", length = 30)
    private NewsCategory category = NewsCategory.NOTICE;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_type", length = 20)
    private NewsletterType newsletterType;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @Column(name = "deleted_at")
    @Getter(AccessLevel.NONE)
    private LocalDateTime deletedAt;

    @Column(name = "search_text", nullable = false, columnDefinition = "TEXT")
    @Getter(AccessLevel.NONE)
    private String searchText = "";

    private News(Post post, NewsCategory category, NewsletterType newsletterType) {
        this.post = post;
        this.category = category == null ? NewsCategory.NOTICE : category;
        this.newsletterType = resolveNewsletterType(this.category, newsletterType);
    }

    public static News create(Post post, NewsCategory category, NewsletterType newsletterType) {
        if (post.getCategory() != PostCategory.NEWS.getValue()) {
            throw new BaseException(ErrorCode.NEWS_INVALID_POST_CATEGORY);
        }
        return new News(post, category, newsletterType);
    }

    public void pin() {
        if (this.isPinned) return;
        this.isPinned = true;
        this.pinnedAt = LocalDateTime.now();
    }

    public void unpin() {
        if (!this.isPinned) return;
        this.isPinned = false;
        this.pinnedAt = null;
    }

    public void changePinned(boolean pinned) {
        if (pinned) {
            pin();
        } else {
            unpin();
        }
    }

    public void change(String title, String content, NewsCategory category, NewsletterType newsletterType) {
        if (title != null) {
            post.changeTitle(title);
        }
        if (content != null) {
            post.changeContent(content);
        }
        if (category != null) {
            this.category = category;
        }
        if (newsletterType != null) {
            this.newsletterType = newsletterType;
        }
        // 뉴스레터가 아니면 세부 분류는 의미가 없으므로 항상 비워 불변식을 유지한다
        this.newsletterType = resolveNewsletterType(this.category, this.newsletterType);
    }

    private static NewsletterType resolveNewsletterType(NewsCategory category, NewsletterType newsletterType) {
        return category == NewsCategory.NEWSLETTER ? newsletterType : null;
    }

    public void changeSearchText(String searchText) {
        this.searchText = searchText == null ? "" : searchText;
    }

    public void softDelete() {
        if (this.post.isDeleted()) return;
        this.post.delete();
    }

    public Long getPostId() {
        return post.getId();
    }

    public Long getAuthorId() {
        return post.getAuthorId();
    }

    public String getTitle() {
        return post.getTitle();
    }

    public String getContent() {
        return post.getContent();
    }

    public LocalDateTime getCreatedAt() {
        return post.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return post.getUpdatedAt();
    }

    public Long getViewCount() {
        return post.getViewCount();
    }

    public NewsCategory getCategory() {
        return category == null ? NewsCategory.NOTICE : category;
    }
}
