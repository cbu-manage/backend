package com.example.cbumanage.news.entity;

import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.news.entity.enums.NewsCategory;
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
                @Index(name = "idx_news_deleted_at", columnList = "deleted_at")
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

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @Column(name = "deleted_at")
    @Getter(AccessLevel.NONE)
    private LocalDateTime deletedAt;

    private News(Post post, NewsCategory category) {
        this.post = post;
        this.category = category == null ? NewsCategory.NOTICE : category;
    }

    public static News create(Post post, NewsCategory category) {
        if (post.getCategory() != PostCategory.NEWS.getValue()) {
            throw new IllegalArgumentException(
                    "News must be created from a Post of category NEWS, but was: " + post.getCategory());
        }
        return new News(post, category);
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

    public void change(String title, String content, NewsCategory category) {
        if (title != null) {
            post.changeTitle(title);
        }
        if (content != null) {
            post.changeContent(content);
        }
        if (category != null) {
            this.category = category;
        }
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
