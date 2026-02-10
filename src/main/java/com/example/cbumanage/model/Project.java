package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.ProjectFieldType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Comment("프로젝트 모집 상세 정보 테이블")
@Table(name = "project")
@NoArgsConstructor
@Getter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("프로젝트 고유 식별자")
    @Column(name = "project_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("연관된 프로젝트 모집 게시글 ID (Post 테이블 참조)")
    @JoinColumn(name = "post_id")
    private Post post;

    @ElementCollection(targetClass = ProjectFieldType.class)
    @CollectionTable(name = "project_recruitment_field", joinColumns = @JoinColumn(name = "project_id"))
    @Enumerated(EnumType.STRING)
    @Comment("현재 프로젝트 모집 여부 (true: 모집중 / false: 마감)")
    @Column(name = "field_name")
    private List<ProjectFieldType> recruitmentFields = new ArrayList<>();

    private boolean recruiting;

    // String 리스트를 받아서 Enum으로 변환해 저장
    public Project(Post post, List<String> fields, boolean recruiting) {
        this.post = post;
        this.recruiting = recruiting;
        if (fields != null) {
            fields.forEach(f -> this.addRecruitmentField(ProjectFieldType.valueOf(f)));
        }
    }

    public static Project create(Post post, List<String> fields, boolean recruiting) {
        return new Project(post, fields, recruiting);
    }

    // 분야 추가 메서드
    public void addRecruitmentField(ProjectFieldType fieldType) {
        this.recruitmentFields.add(fieldType);
    }

    // 수정 메서드
    public void updateRecruitmentFields(List<String> newFields) {
        this.recruitmentFields.clear();
        if (newFields != null) {
            newFields.forEach(f -> this.addRecruitmentField(ProjectFieldType.valueOf(f)));
        }
    }

    public void updateRecruiting(boolean recruiting) {
        this.recruiting = recruiting;
    }
}