package com.example.cbumanage.repository;

import com.example.cbumanage.model.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {

    Study findByPostId(Long postId);

    @EntityGraph(attributePaths = {"post"})
    Page<Study> findByPostCategoryAndPostIsDeletedFalse(int category, Pageable pageable);

    // 태그명 포함 여부로 스터디 목록 조회 (사용자가 추가한 태그 문자열로 검색)
    Page<Study> findByStudyTagsContainingAndPostIsDeletedFalse(String tag, Pageable pageable);
}
