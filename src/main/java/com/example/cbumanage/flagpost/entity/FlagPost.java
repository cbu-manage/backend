package com.example.cbumanage.flagpost.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name="flag_post")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class FlagPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flag_post")
    private Long id;

    private Long authorId;

    private Long postId;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    private boolean isDeleted = false;

    public FlagPost(Long authorId, Long postId, String content) {
        this.authorId = authorId;
        this.postId = postId;
        this.content = content;

    }

    public static FlagPost create(Long authorId, Long postId, String content) {
        return new FlagPost(authorId, postId, content);
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
