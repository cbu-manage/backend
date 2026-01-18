package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.PostReportGroupType;
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

    //운영진의 승인 여부
    private boolean isAccepted;

    //생성자
    public PostReport(Post post, Long groupId,PostReportGroupType type,LocalDateTime date, String location, String reportImage) {
        this.post = post;
        this.groupId = groupId;
        this.type = type;
        this.date = date;
        this.location = location;
        this.reportImage = reportImage;
        this.isAccepted = false;
    }

    //생성 메소드
    public static PostReport create(Post post,Long groupId,PostReportGroupType type ,LocalDateTime date, String location, String reportImage) {
        return new PostReport(post,groupId,type,date, location, reportImage);
    }

    //엔티티 변경 메소드
    public void changeDate(LocalDateTime date) {
        this.date = date;
    }

    public void changeGroup(Group group) {this.groupId = groupId;}

    public void changeType(PostReportGroupType type) {this.type = type;}

    public void changeLocation(String location) {
        this.location = location;
    }

    public void changeReportImage(String reportImage) {}

    public void Accept() {
        this.isAccepted = true;
    }




}
