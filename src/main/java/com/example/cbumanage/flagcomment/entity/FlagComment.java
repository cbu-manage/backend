package com.example.cbumanage.flagcomment.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name="flag_comment")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class FlagComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flag_comment_id")
    private Long id;

    private Long authorId;

    private Long commentId;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    private boolean isDeleted = false;

    public FlagComment(Long authorId, Long commentId, String content) {
        this.authorId = authorId;
        this.commentId = commentId;
        this.content = content;
    }

    public static FlagComment create(Long authorId, Long commentId, String content) {
        return new FlagComment(authorId, commentId, content);
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
