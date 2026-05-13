package com.example.cbumanage.freeboard.entity;


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

    public PostFreeboard(Post post, boolean isAnonymous) {
        this.post = post;
        this.isAnonymous = isAnonymous;
    }

    public static PostFreeboard create(Post post, boolean isAnonymous) {
        return new PostFreeboard(post, isAnonymous);
    }

}
