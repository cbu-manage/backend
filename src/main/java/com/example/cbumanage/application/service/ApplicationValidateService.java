package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicationResultResponse;
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
        // 닉네임까지 일치할 때만 학번 가입 여부를 알려준다.
        // 가입 여부를 먼저 판정하면 닉네임을 몰라도 학번만 바꿔가며 회원 여부를 확인할 수 있다.
        ApplicationValidateResponse response = memberApplicationRepository
                .findByStudentNumberAndNicknameAndStatus(
                        request.studentNumber(),
                        request.nickName(),
                        ApplicationStatus.ADMIN_ACCEPTED)
                .map(ApplicationValidateResponse::from)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));

        userRepository.findByStudentNumber(request.studentNumber()).ifPresent(user -> {
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        });

        return response;
    }

    /**
     * 합격 안내 메일 링크로 여는 화면에서 쓴다. 로그인 전이라 요청자를 알 수 없으므로
     * 메일에 담긴 지원서 UUID로만 찾고, 합격한 지원서가 아니면 알려주지 않는다.
     */
    @Transactional(readOnly = true)
    public ApplicationResultResponse getAcceptedResult(String applicationUuid) {
        return memberApplicationRepository.findByApplicationUuid(applicationUuid)
                .filter(application -> application.getStatus() == ApplicationStatus.ADMIN_ACCEPTED)
                .map(ApplicationResultResponse::from)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));
    }
}
