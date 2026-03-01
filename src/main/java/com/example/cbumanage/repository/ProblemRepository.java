package com.example.cbumanage.repository;

import com.example.cbumanage.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>,
        JpaSpecificationExecutor<Problem> {

    /**
     * 엔티티에서 직접 viewCount를 수정할 경우, race condition 발생시에도 +1이 발생.
     * 따라서 조회 쿼리에서 viewCount로 DB에 직접 수정, DB가 atimic하게 처리하여 동시성 문제 해결
     * @param problemId : Long problemId 이름으로 바인딩.
     */
    @Modifying
    @Query("UPDATE Problem p SET p.viewCount = p.viewCount + 1 WHERE p.problemId = :problemId")
    void incrementViewCount(@Param("problemId") Long problemId);
}
