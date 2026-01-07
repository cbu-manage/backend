package com.example.cbumanage.repository;

import com.example.cbumanage.model.PostProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostProjectRepository extends JpaRepository<PostProject, Long> {
    // Post ID 로 프로젝트 확장 필드 조회
    PostProject findByPostId(Long postId);
}

