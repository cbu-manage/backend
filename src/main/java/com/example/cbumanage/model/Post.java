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

    @ManyToOne
    @JoinColumn(name = "cbu_member_id")
    private CbuMember author;

    private String title;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private int category;

    //생성자
    public Post(CbuMember author,String title,String content,int category) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    //생성 메소드 (Builder 대체)
    public static Post create(CbuMember author,String title,String content,int category) {
        return new Post(author,title,content,category);
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

}
