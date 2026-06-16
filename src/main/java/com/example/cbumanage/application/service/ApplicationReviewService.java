package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.AdminApplicationListResponse;
import com.example.cbumanage.application.dto.ApplicationFinalDecisionUpdateRequest;
import com.example.cbumanage.application.dto.ApplicationDetailResponse;
import com.example.cbumanage.application.dto.ApplicationFinalizeRequest;
import com.example.cbumanage.application.dto.ApplicationListItemResponse;
import com.example.cbumanage.application.entity.ApplicationNotification;
import com.example.cbumanage.application.entity.enums.ApplicationReview;
import com.example.cbumanage.application.entity.enums.FinalDecision;
import com.example.cbumanage.application.entity.enums.MailNotiType;
import com.example.cbumanage.application.dto.RecruitmentResponse;
import com.example.cbumanage.application.dto.RecruitmentSummaryResponse;
import com.example.cbumanage.application.dto.VoteRequest;
import com.example.cbumanage.application.entity.ApplicationVote;
import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.VoteResult;
import com.example.cbumanage.application.repository.ApplicationAnswerRepository;
import com.example.cbumanage.application.repository.ApplicationNotificationRepository;
import com.example.cbumanage.application.repository.ApplicationPortfolioUrlRepository;
import com.example.cbumanage.application.repository.ApplicationVoteRepository;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import com.example.cbumanage.email.dto.EmailAuthResponseDTO;
import com.example.cbumanage.email.service.EmailService;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationReviewService {

    // 투표 자격 운영진 역할 (미투표자 표시 및 진행도 산정)
    private static final List<Role> VOTER_ROLES = Role.applicationVoterRoles();

    private final RecruitmentRepository recruitmentRepository;
    private final MemberApplicationRepository memberApplicationRepository;
    private final ApplicationVoteRepository applicationVoteRepository;
    private final ApplicationAnswerRepository applicationAnswerRepository;
    private final ApplicationPortfolioUrlRepository applicationPortfolioUrlRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ApplicationNotificationRepository applicationNotificationRepository;

    /**
     * 신청서 목록
     * 모집(recruitmentUuid)을 기준으로 generation(기수)·voterCount(전체 신청자)를 잡고,
     * 선택 필터(분야·탭·기간·키워드)로 검색한 뒤 각 행의 투표 진행도(n) 계산
     */
    @Transactional(readOnly = true)
    public AdminApplicationListResponse getApplications(
            String recruitmentUuid, ApplicationField field, ApplicationReview tab,
            LocalDateTime from, LocalDateTime to, String keyword, Pageable pageable) {

        Recruitment recruitment = recruitmentRepository.findByRecruitmentUuid(recruitmentUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));

        List<ApplicationStatus> statuses = (tab == null) ? null : tab.toStatuses();
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<MemberApplication> page = memberApplicationRepository.searchForAdmin(
                recruitment.getGeneration(), field, statuses, from, to, normalizedKeyword, pageable);

        Map<Long, Long> progressByApplicationId = voteProgressByApplicationId(
                page.getContent().stream().map(MemberApplication::getId).toList());

        Page<ApplicationListItemResponse> items = page.map(application ->
                ApplicationListItemResponse.of(
                        application, progressByApplicationId.getOrDefault(application.getId(), 0L)));

        return new AdminApplicationListResponse(recruitment.getVoterCount(), items);
    }

    /**
     * 신청서 ID별 투표 진행도(PASS+FAIL 총 투표 수)를 일괄 집계
     */
    private Map<Long, Long> voteProgressByApplicationId(List<Long> applicationIds) {
        if (applicationIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : applicationVoteRepository.countByApplicationIdsGroupByDecision(applicationIds)) {
            Long applicationId = (Long) row[0];
            long count = (Long) row[2];
            result.merge(applicationId, count, Long::sum);
        }
        return result;
    }

    /**
     * 신청서 상세 + 운영진 투표 현황.
     * 운영진은 서로의 투표 결과와 사유를 열람할 수 있고, 현재 사용자의 투표를 myVote로 내려준다.
     */
    @Transactional(readOnly = true)
    public ApplicationDetailResponse getDetail(String applicationUuid, Long currentUserId) {
        MemberApplication application = memberApplicationRepository.findByApplicationUuid(applicationUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.APPLICATION_NOT_FOUND));

        ApplicationDetailResponse.ApplicantInfo info = new ApplicationDetailResponse.ApplicantInfo(
                application.getApplicationUuid(),
                application.getName(),
                application.getNickname(),
                application.getGrade(),
                application.getStudentNumber(),
                application.getMajor(),
                application.getPhoneNumber(),
                application.getApplicationField(),
                application.getCanOt(),
                application.getCanWelcome(),
                application.getRefSource(),
                application.getRefLinkEtc());

        List<ApplicationDetailResponse.AnswerItem> answers = applicationAnswerRepository
                .findByApplicationId(application.getId()).stream()
                .map(a -> new ApplicationDetailResponse.AnswerItem(a.getQuestionSnapshot(), a.getAnswer()))
                .toList();

        List<ApplicationDetailResponse.PortfolioItem> portfolios = applicationPortfolioUrlRepository
                .findByMemberApplicationIdOrderBySortOrderAsc(application.getId()).stream()
                .map(p -> new ApplicationDetailResponse.PortfolioItem(p.getLabel(), p.getUrl()))
                .toList();

        Map<Long, ApplicationVote> voteByVoterId = applicationVoteRepository
                .findByMemberApplicationId(application.getId()).stream()
                .collect(Collectors.toMap(ApplicationVote::getVoterId, Function.identity()));

        List<ApplicationDetailResponse.VoteItem> votes = userRepository
                .findByRoleInAndDeletedAtIsNull(VOTER_ROLES).stream()
                .map(voter -> {
                    ApplicationVote vote = voteByVoterId.get(voter.getUserId());
                    return new ApplicationDetailResponse.VoteItem(
                            voter.getName(),
                            vote == null ? null : vote.getDecision(),
                            vote == null ? null : vote.getReason());
                })
                .toList();

        ApplicationVote mine = voteByVoterId.get(currentUserId);
        ApplicationDetailResponse.MyVote myVote = new ApplicationDetailResponse.MyVote(
                mine == null ? null : mine.getDecision(),
                mine == null ? null : mine.getReason());

        return new ApplicationDetailResponse(info, answers, portfolios, votes, myVote);
    }

    /**
     * 투표 등록/수정
     * 투표 즉시 화면에 반영
     */
    @Transactional
    public void vote(String applicationUuid, Long currentUserId, VoteRequest request) {
        MemberApplication application = memberApplicationRepository.findByApplicationUuid(applicationUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.APPLICATION_NOT_FOUND));

        User voter = userRepository.findByUserIdAndDeletedAtIsNull(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        if (!voter.getRole().canVoteApplications()) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        if (request.decision() == VoteResult.FAIL
                && (request.reason() == null || request.reason().isBlank())) {
            throw new BaseException(ErrorCode.FAIL_REASON_REQUIRED);
        }
        // PASS는 사유를 보관 X
        String reason = (request.decision() == VoteResult.FAIL) ? request.reason() : null;

        applicationVoteRepository.findByMemberApplicationIdAndVoterId(application.getId(), currentUserId)
                .ifPresentOrElse(
                        existing -> existing.change(request.decision(), reason),
                        () -> applicationVoteRepository.save(ApplicationVote.builder()
                                .memberApplicationId(application.getId())
                                .voterId(currentUserId)
                                .decision(request.decision())
                                .reason(reason)
                                .build()));
    }

    /**
     * 대시보드 요약.
     * 모집 컨텍스트 + 상태별 카운트 + 투표 현황(실시간) + 후보 테이블(SUBMITTED).
     */
    @Transactional(readOnly = true)
    public RecruitmentSummaryResponse getSummary(String recruitmentUuid, Pageable pageable) {
        Recruitment recruitment = recruitmentRepository.findByRecruitmentUuid(recruitmentUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));
        Long generation = recruitment.getGeneration();
        int voterCount = recruitment.getVoterCount();

        // 상태별 카운트
        Map<ApplicationStatus, Long> countByStatus = new EnumMap<>(ApplicationStatus.class);
        for (Object[] row : memberApplicationRepository.countByStatusGroupedForGeneration(generation)) {
            countByStatus.put((ApplicationStatus) row[0], (Long) row[1]);
        }

        // 투표 현황 (CANCELLED 제외 전체)
        List<MemberApplication> reviewable =
                memberApplicationRepository.findByGenerationAndStatusNot(generation, ApplicationStatus.CANCELLED);
        Map<Long, long[]> tallyByApplicationId = passFailByApplicationId(
                reviewable.stream().map(MemberApplication::getId).toList());
        long allPass = 0, allReject = 0, hold = 0;
        for (MemberApplication application : reviewable) {
            long[] passFail = tallyByApplicationId.getOrDefault(application.getId(), new long[2]);
            FinalDecision suggested = suggestDecision(passFail[0], passFail[1], voterCount);
            if (suggested == FinalDecision.ACCEPT) {
                allPass++;
            } else if (suggested == FinalDecision.REJECT) {
                allReject++;
            } else {
                hold++;
            }
        }
        RecruitmentSummaryResponse.VoteCards voteCards =
                new RecruitmentSummaryResponse.VoteCards(reviewable.size(), allPass, hold, allReject);

        // 후보 테이블 (최종결정 대상 = SUBMITTED)
        Slice<MemberApplication> candidateSlice = memberApplicationRepository
                .findByGenerationAndStatusOrderBySubmittedAtDesc(generation, ApplicationStatus.SUBMITTED, pageable);
        Map<Long, long[]> candidateTally = passFailByApplicationId(
                candidateSlice.getContent().stream().map(MemberApplication::getId).toList());
        Slice<RecruitmentSummaryResponse.CandidateRow> candidates = candidateSlice.map(application -> {
            long[] passFail = candidateTally.getOrDefault(application.getId(), new long[2]);
            FinalDecision suggested = suggestDecision(passFail[0], passFail[1], voterCount);
            return new RecruitmentSummaryResponse.CandidateRow(
                    application.getApplicationUuid(),
                    application.getName(),
                    application.getStudentNumber(),
                    application.getMajor(),
                    application.getApplicationField(),
                    passFail[0],
                    passFail[1],
                    suggested);
        });

        return new RecruitmentSummaryResponse(
                RecruitmentResponse.from(recruitment), countByStatus, voteCards, candidates);
    }

    /**
     * 일괄 최종처리.
     * 검토대상(SUBMITTED) 전체가 1. 빠짐없이 결정되고(보류 없음) 2. 투표 완료(n==N)여야 처리된다.
     * ACCEPT는 ADMIN_ACCEPTED, REJECT는 ADMIN_REJECTED로 전이된.
     */
    @Transactional
    public void finalizeDecisions(String recruitmentUuid, Long currentUserId, ApplicationFinalizeRequest request) {
        User decider = userRepository.findByUserIdAndDeletedAtIsNull(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        if (!decider.getRole().isPresidentOrVicePresidentOrAdmin()) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        Recruitment recruitment = recruitmentRepository.findByRecruitmentUuid(recruitmentUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.RECRUITMENT_NOT_FOUND));
        int voterCount = recruitment.getVoterCount();

        List<MemberApplication> targets = memberApplicationRepository.findByGenerationAndStatusIn(
                recruitment.getGeneration(), List.of(ApplicationStatus.SUBMITTED));

        Map<String, FinalDecision> decisionByUuid = request.decisions().stream()
                .collect(Collectors.toMap(
                        ApplicationFinalizeRequest.Item::applicationUuid,
                        ApplicationFinalizeRequest.Item::decision));

        // ① 누락/보류 검증: 모든 대상이 ACCEPT 또는 REJECT로 결정되어야 한다.
        for (MemberApplication application : targets) {
            FinalDecision decision = decisionByUuid.get(application.getApplicationUuid());
            if (decision == null || decision == FinalDecision.HOLD) {
                throw new BaseException(ErrorCode.UNDECIDED_APPLICATION_EXISTS);
            }
        }

        // ② 투표 완료 검증: 모든 대상의 투표 수가 자격자 수(N)와 같아야 한다.
        Map<Long, Long> progressByApplicationId = voteProgressByApplicationId(
                targets.stream().map(MemberApplication::getId).toList());
        for (MemberApplication application : targets) {
            if (progressByApplicationId.getOrDefault(application.getId(), 0L) != voterCount) {
                throw new BaseException(ErrorCode.VOTING_NOT_COMPLETED);
            }
        }

        // ③ 적용
        for (MemberApplication application : targets) {
            FinalDecision decision = decisionByUuid.get(application.getApplicationUuid());
            if (decision == FinalDecision.ACCEPT) {
                application.accept(currentUserId);
                sendResultEmail(application, true);
            } else {
                application.reject(currentUserId, null);
                sendResultEmail(application, false);
            }
        }
    }

    @Transactional
    public void updateFinalDecision(String applicationUuid, Long currentUserId,
                                    ApplicationFinalDecisionUpdateRequest request) {
        User decider = userRepository.findByUserIdAndDeletedAtIsNull(currentUserId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        if (!decider.getRole().isPresidentOrVicePresidentOrAdmin()) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        MemberApplication application = memberApplicationRepository.findByApplicationUuid(applicationUuid)
                .orElseThrow(() -> new BaseException(ErrorCode.APPLICATION_NOT_FOUND));

        if (request.decision() == FinalDecision.ACCEPT) {
            application.accept(currentUserId);
            sendResultEmail(application, true);
            return;
        }
        if (request.decision() == FinalDecision.REJECT) {
            application.reject(currentUserId, request.reason());
            sendResultEmail(application, false);
            return;
        }
        application.hold(currentUserId, request.reason());
    }

    private void sendResultEmail(MemberApplication application, boolean accepted) {
        EmailAuthResponseDTO result = emailService.sendApplicationResultEmail(
                application.getEmail(), application.getName(), accepted);
        if (result == null) {
            result = new EmailAuthResponseDTO(false, "메일 발송 결과를 확인할 수 없습니다.");
        }
        MailNotiType type = accepted ? MailNotiType.ACCEPTED : MailNotiType.REJECTED;
        ApplicationNotification notification = result.isSuccess()
                ? ApplicationNotification.sent(application.getId(), application.getEmail(), type)
                : ApplicationNotification.failed(application.getId(), application.getEmail(), type,
                result.getResponseMessage());
        applicationNotificationRepository.save(notification);
        if (result.isSuccess()) {
            application.markNotified();
        }
    }

    /**
     * 신청서 ID별 [PASS 수, FAIL 수]를 일괄 집계한다.
     */
    private Map<Long, long[]> passFailByApplicationId(List<Long> applicationIds) {
        Map<Long, long[]> result = new HashMap<>();
        if (applicationIds.isEmpty()) {
            return result;
        }
        for (Object[] row : applicationVoteRepository.countByApplicationIdsGroupByDecision(applicationIds)) {
            Long applicationId = (Long) row[0];
            VoteResult decision = (VoteResult) row[1];
            long count = (Long) row[2];
            long[] passFail = result.computeIfAbsent(applicationId, key -> new long[2]);
            if (decision == VoteResult.PASS) {
                passFail[0] += count;
            } else {
                passFail[1] += count;
            }
        }
        return result;
    }

    /**
     * 1차 결과 기준:
     * - 투표 미완료: HOLD
     * - 전원 PASS: ACCEPT
     * - FAIL 과반수: REJECT
     * - 일부 FAIL: HOLD
     */
    private FinalDecision suggestDecision(long passCount, long failCount, int voterCount) {
        long totalVotes = passCount + failCount;
        if (voterCount <= 0 || totalVotes < voterCount) {
            return FinalDecision.HOLD;
        }
        if (passCount == voterCount) {
            return FinalDecision.ACCEPT;
        }
        if (failCount > voterCount / 2) {
            return FinalDecision.REJECT;
        }
        return FinalDecision.HOLD;
    }
}
