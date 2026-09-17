package com.example.cbumanage.suggestion.repository;

import com.example.cbumanage.suggestion.entity.PostSuggestion;
import com.example.cbumanage.suggestion.entity.enums.SuggestionStatus;
import com.example.cbumanage.suggestion.entity.enums.SuggestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 모든 조회에 post 를 함께 가져온다(@EntityGraph). 목록 한 페이지가 행마다 post 를 따로 읽지 않게.
 */
@Repository
public interface PostSuggestionRepository extends JpaRepository<PostSuggestion, Long> {

    @EntityGraph(attributePaths = "post")
    @Query("SELECT s FROM PostSuggestion s WHERE s.post.id = :postId AND s.post.isDeleted = false")
    Optional<PostSuggestion> findActiveByPostId(@Param("postId") Long postId);

    /*
     * 목록 필터 4조합. 파생 쿼리로 두는 이유: null 파라미터를 JPQL 에서 `:x is null or ...` 로 받으면
     * enum 타입 추론이 드라이버·하이버네이트 버전을 타는데, 파생 쿼리는 기동 시점에 검증된다.
     * 정렬은 Pageable 로 넘긴다(isPinned → pinnedAt → post.createdAt).
     */
    @EntityGraph(attributePaths = "post")
    Page<PostSuggestion> findByPost_IsDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = "post")
    Page<PostSuggestion> findByTypeAndPost_IsDeletedFalse(SuggestionType type, Pageable pageable);

    @EntityGraph(attributePaths = "post")
    Page<PostSuggestion> findByStatusAndPost_IsDeletedFalse(SuggestionStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "post")
    Page<PostSuggestion> findByTypeAndStatusAndPost_IsDeletedFalse(
            SuggestionType type, SuggestionStatus status, Pageable pageable);

    /** 마이페이지 — 내가 쓴 건의 */
    @EntityGraph(attributePaths = "post")
    Page<PostSuggestion> findByPost_AuthorIdAndPost_IsDeletedFalse(Long authorId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM PostSuggestion s WHERE s.status = :status AND s.post.isDeleted = false")
    long countByStatus(@Param("status") SuggestionStatus status);
}
