package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 사용자가 자유롭게 추가하는 태그 목록.
     * DB에는 study_tag 테이블에 tag_name VARCHAR로 저장됨 (enum 아님).
     */
    @ElementCollection
    @CollectionTable(name = "study_tag", joinColumns = @JoinColumn(name = "study_id"))
    @Column(name = "tag_name", length = 50)
    private List<String> studyTags = new ArrayList<>();

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
