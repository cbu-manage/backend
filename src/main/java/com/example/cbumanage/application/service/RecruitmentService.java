package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.RecruitmentCreateRequest;
import com.example.cbumanage.application.dto.RecruitmentResponse;
import com.example.cbumanage.application.dto.RecruitmentUpdateRequest;
import com.example.cbumanage.application.dto.CurrentApplicationGenerationResponse;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import com.example.cbumanage.application.repository.ApplicationQuestionRepository;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
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
    private final ApplicationQuestionRepository applicationQuestionRepository;
    private final MemberApplicationRepository memberApplicationRepository;

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

    /**
     * 진행 중인 모집의 투표 자격자 수를 현재 운영진 수로 다시 맞춘다.
     * 운영진이 바뀌어도 N이 고정되면 정원 0명에 1표 같은 상태가 남는다.
     */
    @Transactional
    public void refreshVoterCount() {
        recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN).ifPresent(recruitment -> {
            int voterCount = (int) userRepository.countByRoleInAndDeletedAtIsNull(VOTER_ROLES);
            recruitment.updateVoterCount(voterCount);
        });
    }

    private Long resolveGeneration(RecruitmentCreateRequest request) {
        if (request != null && request.generation() != null) {
            return request.generation();
        }
        return generationPolicy.currentGeneration();
    }

    /**
     * 모집 회차 정보 수정 (기수·기간·발표일). 전달한 필드만 갱신됩니다.
     * 기수를 바꾸면 연결된 지원서 질문·제출된 지원서의 generation도 함께 옮깁니다
     * (FK가 아니라 값이 복제되어 있어 그대로 두면 참조가 끊깁니다).
     */
    @Transactional
    public RecruitmentResponse update(String recruitmentUuid, RecruitmentUpdateRequest request) {
        Recruitment recruitment = recruitmentRepository.findByRecruitmentUuid(recruitmentUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));

        Long newGeneration = request.generation();
        if (newGeneration != null && !newGeneration.equals(recruitment.getGeneration())) {
            recruitmentRepository.findByGeneration(newGeneration).ifPresent(r -> {
                throw new BaseException(ErrorCode.RECRUITMENT_DUPLICATED);
            });
            Long oldGeneration = recruitment.getGeneration();
            applicationQuestionRepository.bulkUpdateGeneration(oldGeneration, newGeneration);
            memberApplicationRepository.bulkUpdateGeneration(oldGeneration, newGeneration);
            recruitment.changeGeneration(newGeneration);
        }

        recruitment.updateSchedule(request.plannedStartDate(), request.plannedEndDate(), request.announcementDate());
        return RecruitmentResponse.from(recruitment);
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
        return recruitmentRepository.findFirstByStatus(RecruitmentStatus.OPEN)
                .map(r -> new CurrentApplicationGenerationResponse(
                        r.getGeneration(), r.getPlannedStartDate(), r.getPlannedEndDate(), r.getAnnouncementDate()))
                .orElseGet(() -> new CurrentApplicationGenerationResponse(
                        generationPolicy.currentGeneration(), null, null, null));
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
