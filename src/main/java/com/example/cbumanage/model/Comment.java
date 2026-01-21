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

    //Post와 Comment는 연관관계가 깊기에 엔티티를 넣었습니다
    @ManyToOne
    @JoinColumn(name="post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    //user와 Comment는 연관관계가 약하기에 userId를 외래키로 넣었습니다
    private Long userId;

    /*
    답글 구조를 위해 댓글-댓글을 1:n 으로 묶고, 연관관계가 강하기에 엔티티로 연결했습니다
     */
    @ManyToOne
    @JoinColumn(name="parent_comment_id",nullable = true)
    private Comment parentComment;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private boolean isDeleted = false;

    /**
     * Post에 대한 댓글 생성
     */
    public Comment(Post post, Long userId,Comment parentComment, String content) {
        this.post = post;
        this.problem = null;
        this.userId = userId;
        this.parentComment = parentComment;
        this.content = content;
    }

    /**
     * Problem 대한 댓글 생성
     */
    public Comment(Problem problem, Long userId,Comment parentComment, String content) {
        this.post = null;
        this.problem = problem;
        this.userId = userId;
        this.parentComment = parentComment;
        this.content = content;
    }

    /**
     * 이  메소드는 생성자 오버로딩 방식으로 변경합니다.
     * 변경 사유:
     * 기존 `create` 메소드는 게시글(Post)에 대한 댓글 생성만 지원했었음.
     * 코딩테스트 문제(Problem)에 대한 댓글 기능이 추가됨에 따라,
     * 각 엔티티에 맞는 댓글을 생성할 수 있도록 public 생성자를 오버로딩하는 방식으로 변경.
     * 사용법:
     * 게시글 댓글 생성: {new Comment(post, userId, parentComment, content)}
     * 문제 댓글 생성: {new Comment(problem, userId, parentComment, content)}
     *
    public static Comment create(Post post, Long userId, Comment parentComment, String content) {
        return new Comment(post, userId, parentComment, content);
    }
     */
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

    //편의메소드
    public void addReply(Comment reply) {
        replies.add(reply);
    }

    public void Delete(){
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
