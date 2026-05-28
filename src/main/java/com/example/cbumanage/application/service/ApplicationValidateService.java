package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicationValidateRequest;
import com.example.cbumanage.application.dto.ApplicationValidateResponse;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationValidateService {

    private final MemberApplicationRepository memberApplicationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ApplicationValidateResponse validate(ApplicationValidateRequest request) {
        userRepository.findByStudentNumber(request.studentNumber()).ifPresent(user -> {
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        });

        return memberApplicationRepository
                .findByStudentNumberAndNicknameAndStatus(
                        request.studentNumber(),
                        request.nickName(),
                        ApplicationStatus.ADMIN_ACCEPTED)
                .map(ApplicationValidateResponse::from)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));
    }
}
