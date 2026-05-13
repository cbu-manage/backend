package com.example.cbumanage.candidate.service;

import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.candidate.dto.SuccessCandidateDTO;
import com.example.cbumanage.candidate.entity.SuccessCandidate;
import com.example.cbumanage.candidate.repository.CandidateManageRepository;
import com.example.cbumanage.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandidateManageService {

    @Autowired
    CandidateManageRepository candidateManageRepository;

    @Autowired
    UserRepository userRepository;

    public SuccessCandidate validateCandidate(SuccessCandidateDTO successCandidateDTO) {
        if(userRepository.findByStudentNumber(successCandidateDTO.getStudentNumber()).isPresent()){
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        }
        SuccessCandidate candidate = candidateManageRepository.findByStudentNumberAndNickName(
                successCandidateDTO.getStudentNumber(), successCandidateDTO.getNickName());
        if (candidate == null) {
            throw new BaseException(ErrorCode.SUCCESS_MEMBER_NOT_FOUND);
        }
        return candidate;
    }

}
