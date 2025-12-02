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

    private Long postId;

    private LocalDateTime date;

    private String location;

    private String startImage;

    private String endImage;

    //생성자
    public PostReport(Long postId, LocalDateTime date, String location, String startImage, String endImage) {
        this.postId = postId;
        this.date = date;
        this.location = location;
        this.startImage = startImage;
        this.endImage = endImage;
    }

    //생성 메소드
    public static PostReport create(Long postId, LocalDateTime date, String location, String startImage, String endImage) {
        return new PostReport(postId, date, location, startImage, endImage);
    }

    //엔티티 변경 메소드
    public void changeDate(LocalDateTime date) {
        this.date = date;
    }

    public void changeLocation(String location) {
        this.location = location;
    }

    public void changeStartImage(String startImage) {
        this.startImage = startImage;
    }

    public void changeEndImage(String endImage) {
        this.endImage = endImage;
    }




}
