package com.example.cbumanage.repository;


import com.example.cbumanage.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByCategoryAndIsDeletedFalse(int category, Pageable pageable);

    Page<Post> findByTitleContainingAndIsDeletedFalse(String title, Pageable pageable);

    Page<Post> findByContentContainingAndIsDeletedFalse(String content, Pageable pageable);

    Page<Post> findByAuthorIdAndIsDeletedFalse(Long authorId,Pageable pageable);


    /**
     * race condition 없이 atomic하게 조회수를 +1 처리합니다.
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
}
