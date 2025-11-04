package com.example.cbumanage.repository;

import com.example.cbumanage.model.SuccessCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuccessCandidateRepository extends JpaRepository<SuccessCandidate, Long> {
    List<SuccessCandidate> findByNickNameIn(List<String> nickNames);

    SuccessCandidate findByStudentNumber(Long studentNumber);

    void deleteByStudentNumber(Long studentNumber);
}
