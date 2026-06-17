package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.RecruitmentCreateRequest;
import com.example.cbumanage.application.dto.RecruitmentResponse;
import com.example.cbumanage.application.dto.CurrentApplicationGenerationResponse;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    // 투표 자격을 가진 역할 (운영진)
    private static final List<Role> VOTER_ROLES = Role.applicationVoterRoles();

    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;
    private final RecruitmentGenerationPolicy generationPolicy;

    /**
     * 모집 시작. 진행 중인 모집이 있으면 거부하고,
     * 시작 시점의 운영진 수를 voterCount(N)로 고정합니다(투표인 수 고정).
     */
    @Transactional
    public RecruitmentResponse open(RecruitmentCreateRequest request) {
        recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN).ifPresent(r -> {
            throw new BaseException(ErrorCode.RECRUITMENT_ALREADY_OPEN);
        });
        Long generation = resolveGeneration(request);
        recruitmentRepository.findByGeneration(generation).ifPresent(r -> {
            throw new BaseException(ErrorCode.RECRUITMENT_DUPLICATED);
        });

        int voterCount = (int) userRepository.countByRoleInAndDeletedAtIsNull(VOTER_ROLES);
        Recruitment recruitment = recruitmentRepository.save(
                Recruitment.open(generation, voterCount));
        return RecruitmentResponse.from(recruitment);
    }

    private Long resolveGeneration(RecruitmentCreateRequest request) {
        if (request != null && request.generation() != null) {
            return request.generation();
        }
        return generationPolicy.currentGeneration();
    }

    /**
     * 모집 마감.
     */
    @Transactional
    public RecruitmentResponse close(String recruitmentUuid) {
        Recruitment recruitment = recruitmentRepository.findByRecruitmentUuid(recruitmentUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));
        if (!recruitment.isOpen()) {
            throw new BaseException(ErrorCode.RECRUITMENT_ALREADY_CLOSED);
        }
        recruitment.close();
        return RecruitmentResponse.from(recruitment);
    }

    /**
     * 현재 진행 중인(OPEN) 모집 조회.
     */
    @Transactional(readOnly = true)
    public RecruitmentResponse getCurrent() {
        Recruitment recruitment = recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));
        return RecruitmentResponse.from(recruitment);
    }

    /**
     * 현재 신청을 받고 있는 모집의 기수 조회.
     */
    @Transactional(readOnly = true)
    public CurrentApplicationGenerationResponse getCurrentApplicationGeneration() {
        Recruitment recruitment = recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));
        return new CurrentApplicationGenerationResponse(recruitment.getGeneration());
    }

    /**
     * 모집 회차 목록을 최신순으로 조회.
     */
    @Transactional(readOnly = true)
    public List<RecruitmentResponse> getAll() {
        return recruitmentRepository.findAllByOrderByStartedAtDesc().stream()
                .map(RecruitmentResponse::from)
                .toList();
    }
}
