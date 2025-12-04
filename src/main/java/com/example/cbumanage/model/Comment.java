package com.example.cbumanage.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name="comment")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="post_id")
    private Post post;

    private Long useId;

    @ManyToOne
    @JoinColumn(name="parent_comment_id",nullable = true)
    private Comment parentComment;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public Comment(Post post, Long userId,Comment parentComment, String content) {
        this.post = post;
        this.useId = userId;
        this.parentComment = parentComment;
        this.content = content;
    }

    public static Comment create(Post post, Long userId, Comment parentComment, String content) {
        return new Comment(post, userId, parentComment, content);
    }

    public void changeContent(String content) {
        this.content = content;
    }

    //댓글-답글 양방향 연결
    @OrderBy("createdAt ASC")
    @OneToMany(mappedBy = "parentComment",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

}
