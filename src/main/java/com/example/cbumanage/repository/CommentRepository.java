package com.example.cbumanage.repository;

import com.example.cbumanage.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    //부모댓글이 없는댓글(답글이 아닌 댓글)만 찾아서 선착순으로 정렬합니다
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);
}
