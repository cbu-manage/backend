package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.ApplicationResultResponse;
import com.example.cbumanage.application.dto.ApplicationValidateRequest;
import com.example.cbumanage.application.dto.ApplicationValidateResponse;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.global.util.RedisUtil;
import com.example.cbumanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ApplicationValidateService {

    /* 학번+닉네임 대입으로 합격자를 훑는 것을 막는다. 지원서 조회(/applications/my)와 같은 기준. */
    private static final String LOOKUP_KEY_PREFIX = "application:validate:";
    private static final long LOOKUP_WINDOW_SECONDS = 60 * 60L;
    private static final long LOOKUP_MAX_PER_WINDOW = 10L;

    private final MemberApplicationRepository memberApplicationRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    @Transactional(readOnly = true)
    public ApplicationValidateResponse validate(ApplicationValidateRequest request) {
        // 닉네임 대입으로 합격자를 찾아내는 것을 막는다. 이 API는 비로그인이라 요청자를 학번으로만 셀 수 있다.
        if (redisUtil.increaseWithExpire(LOOKUP_KEY_PREFIX + request.studentNumber(),
                LOOKUP_WINDOW_SECONDS) > LOOKUP_MAX_PER_WINDOW) {
            throw new BaseException(ErrorCode.APPLICATION_LOOKUP_LIMIT_EXCEEDED);
        }

        // 닉네임까지 일치할 때만 학번 가입 여부를 알려준다.
        // 가입 여부를 먼저 판정하면 닉네임을 몰라도 학번만 바꿔가며 회원 여부를 확인할 수 있다.
        MemberApplication application = memberApplicationRepository
                .findByStudentNumberAndNicknameAndStatus(
                        request.studentNumber(),
                        request.nickName(),
                        ApplicationStatus.ADMIN_ACCEPTED)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));

        // 발표 전에는 합격 여부 자체가 비밀이다. 여기서 200을 주면 회원가입 화면의 "합격자 인증" 버튼
        // 하나로 합불이 갈려, /applications/my 에 걸어둔 발표 전 은닉이 통째로 우회된다.
        if (isBeforeAnnouncement(application.getGeneration())) {
            throw new BaseException(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND);
        }

        userRepository.findByStudentNumber(request.studentNumber()).ifPresent(user -> {
            throw new BaseException(ErrorCode.ALREADY_JOINED_MEMBER);
        });

        return ApplicationValidateResponse.from(application);
    }

    /**
     * 합격 안내 메일 링크로 여는 화면에서 쓴다. 로그인 전이라 요청자를 알 수 없으므로
     * 메일에 담긴 지원서 UUID로만 찾고, 합격한 지원서가 아니면 알려주지 않는다.
     * 발표 전에는 UUID를 알아도 결과를 주지 않는다(합격이면 200, 아니면 404라 상태코드가 곧 합불 신호가 된다).
     */
    @Transactional(readOnly = true)
    public ApplicationResultResponse getAcceptedResult(String applicationUuid) {
        return memberApplicationRepository.findByApplicationUuid(applicationUuid)
                .filter(application -> application.getStatus() == ApplicationStatus.ADMIN_ACCEPTED)
                .filter(application -> !isBeforeAnnouncement(application.getGeneration()))
                .map(ApplicationResultResponse::from)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCEPTED_APPLICATION_NOT_FOUND));
    }

    private boolean isBeforeAnnouncement(Long generation) {
        return recruitmentRepository.findByGeneration(generation)
                .map(recruitment -> recruitment.getAnnouncementDate() == null
                        || LocalDate.now().isBefore(recruitment.getAnnouncementDate()))
                .orElse(false);
    }
}
