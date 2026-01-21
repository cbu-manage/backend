package com.example.cbumanage.repository;

import com.example.cbumanage.model.Project;
import com.example.cbumanage.model.enums.ProjectFieldType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Post ID 로 프로젝트 확장 필드 조회
    Project findByPostId(Long postId);

    // category 번호를 이용하여 프로젝트 게시글 조회
    @EntityGraph(attributePaths = {"post"})
    Page<Project> findByPostCategoryAndPostIsDeletedFalse(int category, Pageable pageable);

    // 프로젝트 모집분야 별로 프로젝트 게시글 조회
    Page<Project> findByRecruitmentFieldsAndPostIsDeletedFalse(ProjectFieldType fields, Pageable pageable);
}

