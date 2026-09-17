package com.example.cbumanage.suggestion.repository;

import com.example.cbumanage.suggestion.entity.PostSuggestion;
import com.example.cbumanage.suggestion.entity.enums.SuggestionStatus;
import com.example.cbumanage.suggestion.entity.enums.SuggestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostSuggestionRepository extends JpaRepository<PostSuggestion, Long> {

    @Query("SELECT s FROM PostSuggestion s WHERE s.post.id = :postId AND s.post.isDeleted = false")
    Optional<PostSuggestion> findActiveByPostId(@Param("postId") Long postId);

    /*
     * 목록 필터 4조합. 파생 쿼리로 두는 이유: null 파라미터를 JPQL 에서 `:x is null or ...` 로 받으면
     * enum 타입 추론이 드라이버·하이버네이트 버전을 타는데, 파생 쿼리는 기동 시점에 검증된다.
     * 정렬은 Pageable 의 "post.createdAt" 으로 넘긴다.
     */
    Page<PostSuggestion> findByPost_IsDeletedFalse(Pageable pageable);

    Page<PostSuggestion> findByTypeAndPost_IsDeletedFalse(SuggestionType type, Pageable pageable);

    Page<PostSuggestion> findByStatusAndPost_IsDeletedFalse(SuggestionStatus status, Pageable pageable);

    Page<PostSuggestion> findByTypeAndStatusAndPost_IsDeletedFalse(
            SuggestionType type, SuggestionStatus status, Pageable pageable);

    @Query("SELECT COUNT(s) FROM PostSuggestion s WHERE s.status = :status AND s.post.isDeleted = false")
    long countByStatus(@Param("status") SuggestionStatus status);
}
