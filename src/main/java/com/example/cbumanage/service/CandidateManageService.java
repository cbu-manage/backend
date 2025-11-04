package com.example.cbumanage.service;

import com.example.cbumanage.authentication.exceptions.MemberExistException;
import com.example.cbumanage.dto.SuccessCandidateDTO;
import com.example.cbumanage.model.SuccessCandidate;
import com.example.cbumanage.repository.CandidateManageRepository;
import com.example.cbumanage.repository.CbuMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandidateManageService {

    @Autowired
    CandidateManageRepository candidateManageRepository;

    @Autowired
    CbuMemberRepository cbuMemberRepository;

    public SuccessCandidate validateCandidate(SuccessCandidateDTO successCandidateDTO) throws Exception {
        if(cbuMemberRepository.findCbuMemberByStudentNumber(successCandidateDTO.getStudentNumber())!=null){
            Exception MemberExistException = new MemberExistException();
            throw MemberExistException;
        }
        return candidateManageRepository.findByStudentNumberAndNickName(successCandidateDTO.getStudentNumber(), successCandidateDTO.getNickName());
    }

}
