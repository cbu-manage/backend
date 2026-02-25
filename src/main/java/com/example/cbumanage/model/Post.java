package com.example.cbumanage.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name="post")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_id")
    private Long id;

    private Long authorId;

    private String title;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private int category;

    private boolean isDeleted = false;

    @Column(nullable = false)
    private Long viewCount = 0L;

    //생성자
    public Post(Long authorId,String title,String content,int category) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    //생성 메소드 (Builder 대체)
    public static Post create(Long authorId,String title,String content,int category) {
        return new Post(authorId,title,content,category);
    }

    /*
    엔티티 변경 메소드 (@Setter 대용)
     */

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void upViewCount() {
        this.viewCount++;
    }

}
