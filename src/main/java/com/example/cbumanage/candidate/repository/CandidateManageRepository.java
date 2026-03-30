package com.example.cbumanage.candidate.repository;

import com.example.cbumanage.candidate.entity.SuccessCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateManageRepository extends JpaRepository<SuccessCandidate, Long> {
    SuccessCandidate findByStudentNumberAndNickName(Long StudentNumber, String NickName);
}
