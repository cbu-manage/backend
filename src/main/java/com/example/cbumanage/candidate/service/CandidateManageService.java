package com.example.cbumanage.candidate.service;

import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.candidate.dto.SuccessCandidateDTO;
import com.example.cbumanage.candidate.entity.SuccessCandidate;
import com.example.cbumanage.candidate.repository.CandidateManageRepository;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandidateManageService {

    @Autowired
    CandidateManageRepository candidateManageRepository;

    @Autowired
    CbuMemberRepository cbuMemberRepository;

    public SuccessCandidate validateCandidate(SuccessCandidateDTO successCandidateDTO) {
        if(cbuMemberRepository.findCbuMemberByStudentNumber(successCandidateDTO.getStudentNumber())!=null){
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        }
        return candidateManageRepository.findByStudentNumberAndNickName(successCandidateDTO.getStudentNumber(), successCandidateDTO.getNickName());
    }

}
