package com.example.cbumanage.suggestion.entity;

import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.suggestion.entity.enums.SuggestionStatus;
import com.example.cbumanage.suggestion.entity.enums.SuggestionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 건의 게시판 부속 정보. 글 본문·작성자·삭제 여부는 {@link Post}(category=9)가 갖고,
 * 여기엔 종류·처리 상태만 둔다. 작성자는 응답에 절대 내려주지 않는다(전부 익명).
 */
@Entity
@NoArgsConstructor
@Getter
@Table(
        name = "post_suggestion",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_suggestion_post", columnNames = "post_id"),
        indexes = {
                @Index(name = "idx_post_suggestion_type", columnList = "suggestion_type"),
                @Index(name = "idx_post_suggestion_status", columnList = "status")
        }
)
public class PostSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_type", length = 20, nullable = false)
    private SuggestionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SuggestionStatus status = SuggestionStatus.OPEN;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    private PostSuggestion(Post post, SuggestionType type) {
        this.post = post;
        this.type = type;
    }

    public static PostSuggestion create(Post post, SuggestionType type) {
        return new PostSuggestion(post, type);
    }

    public void changeType(SuggestionType type) {
        this.type = type;
    }

    /** 상태 전환. 해결로 바뀌는 순간만 resolvedAt 을 찍고, 다시 열면 지운다 */
    public void changeStatus(SuggestionStatus status) {
        if (this.status == status) return;
        this.status = status;
        this.resolvedAt = status == SuggestionStatus.RESOLVED ? LocalDateTime.now() : null;
    }
}
