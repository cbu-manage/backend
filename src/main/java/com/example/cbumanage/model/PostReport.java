package com.example.cbumanage.model;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;


    private LocalDateTime date;

    private String location;

    private String startImage;

    private String endImage;

    private boolean isAccepted;

    //생성자
    public PostReport(Post post, Group group,LocalDateTime date, String location, String startImage, String endImage) {
        this.post = post;
        this.group = group;
        this.date = date;
        this.location = location;
        this.startImage = startImage;
        this.endImage = endImage;
        this.isAccepted = false;
    }

    //생성 메소드
    public static PostReport create(Post post,Group group ,LocalDateTime date, String location, String startImage, String endImage) {
        return new PostReport(post,group,date, location, startImage, endImage);
    }

    //엔티티 변경 메소드
    public void changeDate(LocalDateTime date) {
        this.date = date;
    }

    public void changeGroup(Group group) {this.group = group;}

    public void changeLocation(String location) {
        this.location = location;
    }

    public void changeStartImage(String startImage) {
        this.startImage = startImage;
    }

    public void changeEndImage(String endImage) {
        this.endImage = endImage;
    }

    public void Accept() {
        this.isAccepted = true;
    }




}
