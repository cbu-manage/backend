package com.example.cbumanage.study.entity;

import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.group.entity.enums.GroupRecruitmentStatus;
import com.example.cbumanage.post.entity.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study")
@NoArgsConstructor
@Getter
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    @Comment("스터디 식별자")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @Comment("공통 Post 테이블 FK")
    private Post post;

    @ElementCollection
    @CollectionTable(name = "study_tag", joinColumns = @JoinColumn(name = "study_id"))
    @Column(name = "tag_name", length = 50)
    @Comment("사용자가 입력한 스터디 태그 목록")
    private List<String> studyTags = new ArrayList<>();

    @Column(nullable = false)
    @Comment("스터디 이름")
    private String studyName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Comment("게시글 생성 시 자동 생성된 그룹 FK")
    private Group group;

    public Study(Post post, List<String> tags, String studyName, Group group) {
        this.post = post;
        this.studyName = studyName;
        this.group = group;
        if (tags != null) {
            this.studyTags.addAll(tags);
        }
    }

    public static Study create(Post post, List<String> tags, String studyName, Group group) {
        return new Study(post, tags, studyName, group);
    }

    public void changeStudyName(String studyName) {
        this.studyName = studyName;
    }

    public void updateStudyTags(List<String> newTags) {
        this.studyTags.clear();
        if (newTags != null) {
            this.studyTags.addAll(newTags);
        }
    }

    public boolean isRecruiting() {
        return group.getRecruitmentStatus() == GroupRecruitmentStatus.OPEN;
    }

    public void updateRecruiting(boolean recruiting) {
        if (recruiting) {
            group.openRecruitment();
        } else {
            group.closeRecruitment();
        }
    }
}
