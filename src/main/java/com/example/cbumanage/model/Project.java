package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project")
@NoArgsConstructor
@Getter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    // 게시글 기본 정보와 1:1로 연결해 확장 필드를 저장
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // 모집 분야(예: 프론트엔드, 백엔드)
    private String recruitmentField;

    // 모집 진행 여부
    private boolean recruiting;

    // 생성자
    public Project(Post post, String recruitmentField, boolean recruiting) {
        this.post = post;
        this.recruitmentField = recruitmentField;
        this.recruiting = recruiting;
    }

    // 생성 메서드
    public static Project create(Post post, String recruitmentField, boolean recruiting) {
        return new Project(post, recruitmentField, recruiting);
    }

    //변경 메소드
    public void changeRecruitmentField(String recruitmentField) {
        this.recruitmentField = recruitmentField;
    }

    public void changeRecruiting(boolean recruiting) {
        this.recruiting = recruiting;
    }
}