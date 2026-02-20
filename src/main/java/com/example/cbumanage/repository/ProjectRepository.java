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
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByPostId(Long postId);

    @Query("select p from Project p where p.group.id = :groupId")
    Optional<Project> findByGroupId(@Param("groupId") Long groupId);

    // 프로젝트 게시글 전체 조회 및 모집여부 필터
    @Query(value = "SELECT p FROM Project p " +
            "JOIN FETCH p.post po " +
            "JOIN FETCH p.member m " +
            "WHERE po.isDeleted = false " +
            "AND po.category = :category " +
            "AND (:recruiting IS NULL OR p.recruiting = :recruiting)",
            countQuery = "SELECT count(p) FROM Project p " +
                    "JOIN p.post po " +
                    "WHERE po.isDeleted = false " +
                    "AND po.category = :category " +
                    "AND (:recruiting IS NULL OR p.recruiting = :recruiting)")
    Page<Project> findByCategory(@Param("category") int category,
                                 @Param("recruiting") Boolean recruiting,
                                 Pageable pageable);

    // 프로젝트 게시글 모집분야별로 조회 및 모집여부 필터
    @Query(value = "SELECT p FROM Project p " +
            "JOIN FETCH p.post po " +
            "JOIN FETCH p.member m " +
            "WHERE po.isDeleted = false " +
            "AND :fields MEMBER OF p.recruitmentFields " +
            "AND (:recruiting IS NULL OR p.recruiting = :recruiting)",
            countQuery = "SELECT count(p) FROM Project p " +
                    "JOIN p.post po " +
                    "WHERE po.isDeleted = false " +
                    "AND :fields MEMBER OF p.recruitmentFields " +
                    "AND (:recruiting IS NULL OR p.recruiting = :recruiting)")
    Page<Project> findByFilters(@Param("fields") ProjectFieldType fields,
                                @Param("recruiting") Boolean recruiting,
                                Pageable pageable);

    // 내가 작성한 프로젝트 게시글 전체 조회
    @Query(value = "SELECT p FROM Project p " +
            "JOIN FETCH p.post po " +
            "JOIN FETCH p.member m " +
            "WHERE po.isDeleted = false " +
            "AND m.cbuMemberId = :userId " +
            "AND po.category = :category",
            countQuery = "SELECT count(p) FROM Project p " +
                    "JOIN p.post po " +
                    "WHERE po.isDeleted = false " +
                    "AND m.cbuMemberId = :userId " +
                    "AND po.category = :category")
    Page<Project> findByUserIdAndCategory(@Param("userId") Long userId,
                                          @Param("category") int category,
                                          Pageable pageable);
}

