package com.example.cbumanage.report.entity;

import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.report.entity.enums.PostReportGroupType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name="post_report")
@EnableJpaAuditing
public class PostReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_report_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="post_id")
    private Post post;

    //보고서를 작성한 그룹
    private Long groupId;

    @Enumerated(EnumType.STRING)
    private PostReportGroupType type;

    private LocalDateTime date;

    private String location;

    private String reportImage;

    @Column(columnDefinition = "TEXT")
    private String reflection;

    @Column(columnDefinition = "TEXT")
    private String nextPlan;

    //운영진의 승인 여부
    private boolean isAccepted = false;

    //생성자
    public PostReport(Post post, Long groupId, PostReportGroupType type, LocalDateTime date, String location, String reportImage, String reflection, String nextPlan) {
        this.post = post;
        this.groupId = groupId;
        this.type = type;
        this.date = date;
        this.location = location;
        this.reportImage = reportImage;
        this.reflection = reflection;
        this.nextPlan = nextPlan;
    }

    //생성 메소드
    public static PostReport create(Post post, Long groupId, PostReportGroupType type, LocalDateTime date, String location, String reportImage, String reflection, String nextPlan) {
        return new PostReport(post, groupId, type, date, location, reportImage, reflection, nextPlan);
    }

    //엔티티 변경 메소드
    public void changeDate(LocalDateTime date) {
        this.date = date;
    }

    public void changeGroup(Group group) {this.groupId = group.getId();}

    public void changeType(PostReportGroupType type) {this.type = type;}

    public void changeLocation(String location) {
        this.location = location;
    }

    public void changeReportImage(String reportImage) {
        this.reportImage = reportImage;
    }

    public void changeReflection(String reflection) {
        this.reflection = reflection;
    }

    public void changeNextPlan(String nextPlan) {
        this.nextPlan = nextPlan;
    }

    public void Accept() {
        this.isAccepted = true;
    }




}
