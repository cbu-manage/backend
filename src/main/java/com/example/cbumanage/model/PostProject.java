package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_project")
@NoArgsConstructor
@Getter
public class PostProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_project_id")
    private Long id;

    // 게시글 기본 정보와 1:1로 연결해 확장 필드를 저장
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // 모집 분야(예: 프론트엔드, 백엔드)
    private String recruitmentField;

    // 요구 기술 스택
    private String techStack;

    // 모집 진행 여부
    private boolean recruiting;

    // 생성자
    public PostProject(Post post, String recruitmentField, String techStack, boolean recruiting) {
        this.post = post;
        this.recruitmentField = recruitmentField;
        this.techStack = techStack;
        this.recruiting = recruiting;
    }

    // 생성 메서드
    public static PostProject create(Post post, String recruitmentField, String techStack, boolean recruiting) {
        return new PostProject(post, recruitmentField, techStack, recruiting);
    }

    //변경 메소드
    public void changeRecruitmentField(String recruitmentField) {
        this.recruitmentField = recruitmentField;
    }

    public void changeTechStack(String techStack) {
        this.techStack = techStack;
    }

    public void changeRecruiting(boolean recruiting) {
        this.recruiting = recruiting;
    }
}