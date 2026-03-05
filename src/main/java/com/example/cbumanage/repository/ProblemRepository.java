package com.example.cbumanage.repository;

import com.example.cbumanage.model.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>,
        JpaSpecificationExecutor<Problem> {

    Page<Problem> findByPostAuthorId(Long authorId, Pageable pageable);
}
