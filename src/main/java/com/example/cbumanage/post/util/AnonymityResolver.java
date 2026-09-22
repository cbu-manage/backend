package com.example.cbumanage.post.util;

import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.freeboard.entity.PostFreeboard;
import com.example.cbumanage.freeboard.repository.PostFreeboardRepository;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 글·댓글의 익명 여부 판정. 신고 응답에서 작성자를 가릴지 결정하는 기준이다. */
@Component
@RequiredArgsConstructor
public class AnonymityResolver {

    private final PostFreeboardRepository postFreeboardRepository;

    /** 건의는 항상 익명, 자유게시판은 글마다 선택, 나머지는 공개다. */
    public boolean isAnonymous(Post post) {
        if (post == null) return false;
        if (post.getCategory() == PostCategory.SUGGESTION.getValue()) return true;
        if (post.getCategory() == PostCategory.FREEBOARD.getValue()) {
            return postFreeboardRepository.findByPostId(post.getId())
                    .map(PostFreeboard::isAnonymous)
                    .orElse(false);
        }
        return false;
    }

    /** 익명 댓글이거나 건의글에 달린 댓글이면 익명이다. */
    public boolean isAnonymous(Comment comment) {
        if (comment == null) return false;
        if (comment.isAnonymous()) return true;
        Post post = comment.getPost();
        return post != null && post.getCategory() == PostCategory.SUGGESTION.getValue();
    }
}
