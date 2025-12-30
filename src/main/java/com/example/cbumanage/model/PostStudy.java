package com.example.cbumanage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@Entity
@NoArgsConstructor
@Getter
@Table(name="post_study")
@EnableJpaAuditing
public class PostStudy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_study_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="post_id")
    private Post post;

    private Boolean status;

    //생성자
    public PostStudy(Post post, Boolean status) {
        this.post = post;
        this.status = status;
    }

    //생성 메소드
    public static PostStudy create(Post post, Boolean status) {
        return new PostStudy(post, status);
    }

    //엔티티 변경 메서드
    public void changeStatus(Boolean status) {
        this.status = status;
    }
}

