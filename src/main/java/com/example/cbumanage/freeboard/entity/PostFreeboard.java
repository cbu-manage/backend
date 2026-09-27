package com.example.cbumanage.freeboard.entity;


import com.example.cbumanage.freeboard.entity.enums.FreeboardTopic;
import com.example.cbumanage.post.entity.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name="post_freeboard")
public class PostFreeboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private boolean isAnonymous;

    /** 말머리. 이 기능 이전에 쓰인 글은 값이 없어 null 을 허용한다. */
    @Enumerated(EnumType.STRING)
    @Column(name = "topic", length = 20)
    private FreeboardTopic topic;

    public PostFreeboard(Post post, boolean isAnonymous, FreeboardTopic topic) {
        this.post = post;
        this.isAnonymous = isAnonymous;
        this.topic = topic;
    }

    public static PostFreeboard create(Post post, boolean isAnonymous, FreeboardTopic topic) {
        return new PostFreeboard(post, isAnonymous, topic);
    }

    public void changeTopic(FreeboardTopic topic) {
        this.topic = topic;
    }

}
