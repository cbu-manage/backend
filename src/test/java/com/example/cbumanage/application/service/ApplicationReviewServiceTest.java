package com.example.cbumanage.application.service;

import com.example.cbumanage.application.dto.AdminApplicationListResponse;
import com.example.cbumanage.application.dto.ApplicationDetailResponse;
import com.example.cbumanage.application.entity.ApplicationVote;
import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationReview;
import com.example.cbumanage.application.entity.enums.FinalDecision;
import com.example.cbumanage.application.entity.enums.RefSource;
import com.example.cbumanage.application.entity.enums.VoteResult;
import com.example.cbumanage.application.repository.ApplicationAnswerRepository;
import com.example.cbumanage.application.repository.ApplicationNotificationRepository;
import com.example.cbumanage.application.repository.ApplicationPortfolioUrlRepository;
import com.example.cbumanage.application.repository.ApplicationVoteRepository;
import com.example.cbumanage.application.repository.MemberApplicationRepository;
import com.example.cbumanage.application.repository.RecruitmentRepository;
import com.example.cbumanage.email.service.EmailService;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationReviewServiceTest {

    private final RecruitmentRepository recruitmentRepository = mock(RecruitmentRepository.class);
    private final MemberApplicationRepository memberApplicationRepository = mock(MemberApplicationRepository.class);
    private final ApplicationVoteRepository applicationVoteRepository = mock(ApplicationVoteRepository.class);
    private final ApplicationAnswerRepository applicationAnswerRepository = mock(ApplicationAnswerRepository.class);
    private final ApplicationPortfolioUrlRepository applicationPortfolioUrlRepository = mock(ApplicationPortfolioUrlRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ApplicationReviewService applicationReviewService = new ApplicationReviewService(
            recruitmentRepository,
            memberApplicationRepository,
            applicationVoteRepository,
            applicationAnswerRepository,
            applicationPortfolioUrlRepository,
            userRepository,
            mock(EmailService.class),
            mock(ApplicationNotificationRepository.class)
    );

    @Test
    void detailShowsOtherVotersDecisionEvenBeforeAllVotesAreCompleted() {
        MemberApplication application = application(10L, 40L);
        User owner = voter(1L, "회장", Role.ROLE_PRESIDENT);
        User manager = voter(2L, "운영진", Role.ROLE_MANAGER);
        ApplicationVote managerVote = vote(10L, 2L, VoteResult.FAIL, "경험 부족");

        stubDetail(application, List.of(owner, manager), List.of(managerVote), 2);

        ApplicationDetailResponse response = applicationReviewService.getDetail(application.getApplicationUuid(), 1L);

        assertThat(response.votes()).hasSize(2);
        assertThat(response.votes().get(0).decision()).isNull();
        assertThat(response.votes().get(1).voterName()).isEqualTo("운영진");
        assertThat(response.votes().get(1).decision()).isEqualTo(VoteResult.FAIL);
        assertThat(response.votes().get(1).reason()).isEqualTo("경험 부족");
    }

    @Test
    void detailRevealsVotesAfterAllVotesAreCompleted() {
        MemberApplication application = application(10L, 40L);
        User owner = voter(1L, "회장", Role.ROLE_PRESIDENT);
        User manager = voter(2L, "운영진", Role.ROLE_MANAGER);
        ApplicationVote ownerVote = vote(10L, 1L, VoteResult.PASS, null);
        ApplicationVote managerVote = vote(10L, 2L, VoteResult.FAIL, "경험 부족");

        stubDetail(application, List.of(owner, manager), List.of(ownerVote, managerVote), 2);

        ApplicationDetailResponse response = applicationReviewService.getDetail(application.getApplicationUuid(), 1L);

        assertThat(response.votes()).extracting(ApplicationDetailResponse.VoteItem::decision)
                .containsExactly(VoteResult.PASS, VoteResult.FAIL);
        assertThat(response.votes().get(1).reason()).isEqualTo("경험 부족");
    }

    @Test
    void listIncludesVoteCountsMyReviewAttendanceAndApplicationHistoryNote() {
        Recruitment recruitment = Recruitment.open(40L, 3);
        MemberApplication current = application(10L, 40L);
        MemberApplication previous = application(9L, 39L);
        ApplicationVote myVote = vote(10L, 1L, VoteResult.PASS, null);

        when(recruitmentRepository.findByRecruitmentUuid(recruitment.getRecruitmentUuid()))
                .thenReturn(Optional.of(recruitment));
        when(memberApplicationRepository.searchForAdmin(
                40L, null, null, null, null, null, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(current), PageRequest.of(0, 10), 1));
        when(applicationVoteRepository.countByApplicationIdsGroupByDecision(List.of(10L)))
                .thenReturn(List.of(
                        new Object[]{10L, VoteResult.PASS, 2L},
                        new Object[]{10L, VoteResult.FAIL, 1L}
                ));
        when(applicationVoteRepository.findByMemberApplicationIdInAndVoterId(List.of(10L), 1L))
                .thenReturn(List.of(myVote));
        when(memberApplicationRepository.findByStudentNumberIn(List.of(2024000001L)))
                .thenReturn(List.of(previous, current));

        AdminApplicationListResponse response = applicationReviewService.getApplications(
                recruitment.getRecruitmentUuid(),
                null,
                ApplicationReview.ALL,
                null,
                null,
                null,
                PageRequest.of(0, 10),
                1L);

        assertThat(response.voterCount()).isEqualTo(3);
        assertThat(response.applications().getTotalElements()).isEqualTo(1);
        var item = response.applications().getContent().get(0);
        assertThat(item.passCount()).isEqualTo(2);
        assertThat(item.failCount()).isEqualTo(1);
        assertThat(item.voteProgress()).isEqualTo(3);
        assertThat(item.myReviewed()).isTrue();
        assertThat(item.canOt()).isTrue();
        assertThat(item.canWelcome()).isTrue();
        assertThat(item.finalDecision()).isNull();
        assertThat(item.suggestedDecision()).isEqualTo(FinalDecision.HOLD);
        assertThat(item.note()).isEqualTo("39, 40기에 지원");
    }

    private void stubDetail(MemberApplication application, List<User> voters,
                            List<ApplicationVote> votes, int voterCount) {
        when(memberApplicationRepository.findByApplicationUuid(application.getApplicationUuid()))
                .thenReturn(Optional.of(application));
        when(applicationAnswerRepository.findByApplicationId(application.getId())).thenReturn(List.of());
        when(applicationPortfolioUrlRepository.findByMemberApplicationIdOrderBySortOrderAsc(application.getId()))
                .thenReturn(List.of());
        when(applicationVoteRepository.findByMemberApplicationId(application.getId())).thenReturn(votes);
        when(userRepository.findByRoleInAndDeletedAtIsNull(Role.applicationVoterRoles())).thenReturn(voters);
        when(recruitmentRepository.findByGeneration(application.getGeneration()))
                .thenReturn(Optional.of(Recruitment.open(application.getGeneration(), voterCount)));
    }

    private static MemberApplication application(Long id, Long generation) {
        MemberApplication application = MemberApplication.builder()
                .studentNumber(2024000001L)
                .email("applicant@example.com")
                .name("홍길동")
                .nickname("cbu")
                .grade(AcademicStatus.JUNIOR)
                .major("컴퓨터공학과")
                .phoneNumber("010-1234-5678")
                .generation(generation)
                .applicationField(ApplicationField.PROJECT)
                .portfolioUrl("https://github.com/cbu")
                .refSource(RefSource.FRIEND)
                .refLinkEtc(null)
                .canOt(true)
                .canWelcome(true)
                .privacyPolicy(true)
                .build();
        ReflectionTestUtils.setField(application, "id", id);
        return application;
    }

    private static User voter(Long userId, String name, Role role) {
        User user = new User(name + "@example.com", 2024000000L + userId, "encoded-password");
        ReflectionTestUtils.setField(user, "userId", userId);
        ReflectionTestUtils.setField(user, "name", name);
        user.changeRole(role);
        return user;
    }

    private static ApplicationVote vote(Long applicationId, Long voterId,
                                        VoteResult decision, String reason) {
        return ApplicationVote.builder()
                .memberApplicationId(applicationId)
                .voterId(voterId)
                .decision(decision)
                .reason(reason)
                .build();
    }
}
