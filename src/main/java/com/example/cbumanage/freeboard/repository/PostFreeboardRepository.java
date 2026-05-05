package com.example.cbumanage.freeboard.repository;

import com.example.cbumanage.freeboard.entity.PostFreeboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostFreeboardRepository extends JpaRepository<PostFreeboard, Long> {

    Optional<PostFreeboard> findByPostId(Long postId);

    @Query("SELECT pf FROM PostFreeboard pf WHERE pf.post.isDeleted = false")
    Page<PostFreeboard> findAllActive(Pageable pageable);

    @Query("SELECT pf FROM PostFreeboard pf WHERE pf.post.authorId = :authorId AND pf.post.isDeleted = false")
    Page<PostFreeboard> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
}
