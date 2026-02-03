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

    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.post " +
            "WHERE p.post.isDeleted = false " +
            "AND p.post.category = :category " +
            "AND (:recruiting IS NULL OR p.recruiting = :recruiting)")
    Page<Project> findByCategory(@Param("category") int category,
                                            @Param("recruiting") Boolean recruiting,
                                            Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.post.isDeleted = false " +
            "AND :fields MEMBER OF p.recruitmentFields " +
            "AND (:recruiting IS NULL OR p.recruiting = :recruiting)")
    Page<Project> findByFilters(@Param("fields") ProjectFieldType fields,
                                @Param("recruiting") Boolean recruiting,
                                Pageable pageable);
}

