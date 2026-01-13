package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_study")
@NoArgsConstructor
@Getter
public class PostStudy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_study_id")
    private Long id;

    // 게시글 기본 정보와 1:1로 연결해 확장 필드를 저장
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // 모집 진행 여부
    private boolean status;

    // 생성자
    public PostStudy(Post post, boolean status) {
        this.post = post;
        this.status = status;
    }

    // 생성 메서드
    public static PostStudy create(Post post, boolean status) {
        return new PostStudy(post, status);
    }

    //변경 메소드
    public void changeStatus(boolean status) {
        this.status = status;
    }
}

