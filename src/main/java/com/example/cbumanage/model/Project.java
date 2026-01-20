package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.ProjectFieldType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@NoArgsConstructor
@Getter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ElementCollection(targetClass = ProjectFieldType.class)
    @CollectionTable(name = "project_recruitment_field", joinColumns = @JoinColumn(name = "project_id"))
    @Enumerated(EnumType.STRING)
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