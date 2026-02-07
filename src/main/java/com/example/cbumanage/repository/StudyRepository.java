package com.example.cbumanage.repository;

import com.example.cbumanage.model.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {

    Study findByPostId(Long postId);

    @EntityGraph(attributePaths = {"post"})
    Page<Study> findByPostCategoryAndPostIsDeletedFalse(int category, Pageable pageable);

    @Query("""
                SELECT s
                FROM Study s
                JOIN s.studyTags t
                WHERE t = :tag
                  AND s.post.isDeleted = false
            """)
    Page<Study> findByExactTagAndPostIsDeletedFalse(@Param("tag") String tag, Pageable pageable);
}
