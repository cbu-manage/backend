package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.ApplicationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 지원서 질문 레포지토리
 */
public interface ApplicationQuestionRepository extends JpaRepository<ApplicationQuestion, Long> {

    // UUID로 단건 조회 (외부 API path variable)
    Optional<ApplicationQuestion> findByQuestionUuid(String questionUuid);

    /**
     * 기수별 질문 목록, 신청서 작성 화면에서 호출
     */
    List<ApplicationQuestion> findByGenerationAndDeletedAtIsNullOrderBySortOrderAsc(Long generation);

    /**
     * 한 기수의 최댓값 sort_order 조회
     */
    @Query("""
           SELECT MAX(q.sortOrder) FROM ApplicationQuestion q
           WHERE q.generation = :generation
           """)
    Optional<Integer> findMaxSortOrderByGeneration(@Param("generation") Long generation);

    // (generation, type) 유니크 제약 위반을 사전에 확인 (deleted_at 무관, DB 제약과 동일 기준)
    boolean existsByGenerationAndType(Long generation, String type);

    // Recruitment의 기수 번호 변경 시 연결된 질문의 generation을 함께 옮긴다 (FK 아니라 값 복제)
    @Modifying
    @Query("UPDATE ApplicationQuestion q SET q.generation = :to WHERE q.generation = :from")
    int bulkUpdateGeneration(@Param("from") Long from, @Param("to") Long to);
}
