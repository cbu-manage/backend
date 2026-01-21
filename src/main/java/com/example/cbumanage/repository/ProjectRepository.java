package com.example.cbumanage.repository;

import com.example.cbumanage.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Post ID 로 프로젝트 확장 필드 조회
    Project findByPostId(Long postId);

    // category 번호를 이용하여 프로젝트 게시글 필드 조회
    @Query("SELECT p FROM Project p JOIN FETCH p.post post " +
            "WHERE post.category = :category AND post.isDeleted = false")
    Page<Project> findAllWithPostByCategory(@Param("category") int category, Pageable pageable);
}

