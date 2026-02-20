package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.ProjectFieldType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import java.time.LocalDate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("연관된 프로젝트 모집 그룹 ID (Group 테이블 참조)")
    @JoinColumn(name = "group_id",nullable = false)
    private Group group;

    @ElementCollection(targetClass = ProjectFieldType.class)
    @CollectionTable(name = "project_recruitment_field", joinColumns = @JoinColumn(name = "project_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "field_name")
    private List<ProjectFieldType> recruitmentFields = new ArrayList<>();

    @Comment("현재 프로젝트 모집 여부 (true: 모집중 / false: 모집완료)")
    private boolean recruiting;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "마감일은 오늘 이후여야 합니다.") // 과거 날짜 방지 (선택)
    @Column(nullable = true)
    private LocalDate deadline;

    // String 리스트를 받아서 Enum으로 변환해 저장
    public Project(Post post, List<String> fields, boolean recruiting,LocalDate deadline, Group group) {
        this.post = post;
        this.recruiting = recruiting;
        this.deadline = deadline;
        if (fields != null) {
            fields.forEach(f -> this.addRecruitmentField(ProjectFieldType.valueOf(f)));
        }
        this.group=group;
    }

    public static Project create(Post post, List<String> fields, boolean recruiting, LocalDate deadline, Group group) {
        return new Project(post, fields, recruiting, deadline, group);
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

    public void updateDeadline(LocalDate deadline) {this.deadline = deadline;}
}