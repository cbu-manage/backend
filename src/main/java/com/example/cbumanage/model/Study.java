package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study")
@NoArgsConstructor
@Getter
@Schema(description = "스터디 모집 서브 엔티티. 공통 Post와 1:1로 연결되며 태그와 모집 여부를 저장합니다.")
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    @Schema(description = "스터디 식별자")
    @Comment("스터디 식별자")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @Schema(description = "연결된 공통 Post 엔티티")
    @Comment("공통 Post 테이블 FK")
    private Post post;

    /**
     * 사용자가 자유롭게 추가하는 태그 목록.
     * DB에는 study_tag 테이블에 tag_name VARCHAR로 저장됨 (enum 아님).
     */
    @ElementCollection
    @CollectionTable(name = "study_tag", joinColumns = @JoinColumn(name = "study_id"))
    @Column(name = "tag_name", length = 50)
    @Schema(description = "사용자가 자유롭게 입력한 스터디 태그 목록")
    @Comment("사용자가 입력한 스터디 태그 목록")
    private List<String> studyTags = new ArrayList<>();

    @Schema(description = "모집 중 여부 (true=모집 중, false=모집 완료)")
    @Comment("모집 중 여부 (true=모집 중, false=모집 완료)")
    private boolean recruiting;

    public Study(Post post, List<String> tags, boolean recruiting) {
        this.post = post;
        this.recruiting = recruiting;
        if (tags != null) {
            this.studyTags.addAll(tags);
        }
    }

    public static Study create(Post post, List<String> tags, boolean recruiting) {
        return new Study(post, tags, recruiting);
    }

    public void addStudyTag(String tag) {
        this.studyTags.add(tag);
    }

    public void updateStudyTags(List<String> newTags) {
        this.studyTags.clear();
        if (newTags != null) {
            this.studyTags.addAll(newTags);
        }
    }

    public void updateRecruiting(boolean recruiting) {
        this.recruiting = recruiting;
    }
}
