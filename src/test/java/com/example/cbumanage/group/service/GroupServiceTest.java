package com.example.cbumanage.group.service;

import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.group.entity.GroupMember;
import com.example.cbumanage.group.entity.enums.GroupMemberRole;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.group.repository.GroupMemberRepository;
import com.example.cbumanage.group.repository.GroupRepository;
import com.example.cbumanage.group.util.GroupUtil;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.project.repository.ProjectRepository;
import com.example.cbumanage.study.repository.StudyRepository;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupServiceTest {

    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final GroupMemberRepository groupMemberRepository = mock(GroupMemberRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final StudyRepository studyRepository = mock(StudyRepository.class);

    private final GroupService groupService = new GroupService(
            groupRepository,
            groupMemberRepository,
            mock(UserRepository.class),
            mock(CommentRepository.class),
            projectRepository,
            studyRepository,
            mock(GroupUtil.class)
    );

    private static final Long GROUP_ID = 1L;
    private static final Long LEADER_ID = 10L;
    private static final Long MEMBER_ID = 20L;

    private static User user(String email) {
        return new User(email, 20240001L, "encoded");
    }

    private GroupMember leaderOf(Group group) {
        GroupMember leader = GroupMember.create(group, user("leader@example.com"),
                GroupMemberStatus.ACTIVE, GroupMemberRole.LEADER);
        when(groupMemberRepository.findByGroupIdAndUserUserId(GROUP_ID, LEADER_ID)).thenReturn(leader);
        return leader;
    }

    private Group group() {
        Group group = Group.create("테스트 그룹", 1, 5, 100L, 0);
        // 영속화되지 않은 엔티티는 id가 null이라 리더 조회가 빗나간다
        ReflectionTestUtils.setField(group, "id", GROUP_ID);
        when(groupRepository.findByIdAndIsDeletedFalse(GROUP_ID)).thenReturn(Optional.of(group));
        when(projectRepository.findByGroupId(GROUP_ID)).thenReturn(Optional.empty());
        when(studyRepository.findByGroupId(GROUP_ID)).thenReturn(Optional.empty());
        return group;
    }

    private GroupMember applicantIn(Group group, Long groupMemberId) {
        GroupMember applicant = GroupMember.create(group, user("member@example.com"),
                GroupMemberStatus.PENDING, GroupMemberRole.MEMBER);
        when(groupMemberRepository.findById(groupMemberId)).thenReturn(Optional.of(applicant));
        return applicant;
    }

    @Test
    void 활동중단으로_바꾸면_거절로_저장되지_않고_재신청_횟수도_오르지_않는다() {
        Group group = group();
        leaderOf(group);
        GroupMember applicant = applicantIn(group, 50L);

        groupService.updateStatusGroupMember(50L, LEADER_ID, GroupMemberStatus.INACTIVE, null);

        assertThat(applicant.getGroupMemberStatus()).isEqualTo(GroupMemberStatus.INACTIVE);
        assertThat(applicant.getRejectedCount()).isZero();
        assertThat(applicant.getMemberRejectReason()).isNull();
    }

    @Test
    void 거절로_바꿀_때만_재신청_횟수가_오른다() {
        Group group = group();
        leaderOf(group);
        GroupMember applicant = applicantIn(group, 51L);

        groupService.updateStatusGroupMember(51L, LEADER_ID, GroupMemberStatus.REJECTED, "이번엔 어려워요");

        assertThat(applicant.getGroupMemberStatus()).isEqualTo(GroupMemberStatus.REJECTED);
        assertThat(applicant.getRejectedCount()).isEqualTo(1);
    }

    @Test
    void 팀장은_자기_상태를_바꿀_수_없다() {
        Group group = group();
        GroupMember leader = leaderOf(group);
        when(groupMemberRepository.findById(60L)).thenReturn(Optional.of(leader));

        assertThatThrownBy(() -> groupService.updateStatusGroupMember(
                60L, LEADER_ID, GroupMemberStatus.REJECTED, "실수"))
                .isInstanceOf(BaseException.class)
                .extracting(e -> ((BaseException) e).getErrorCode())
                .isEqualTo(ErrorCode.GROUP_LEADER_STATUS_IMMUTABLE);

        assertThat(leader.getGroupMemberStatus()).isEqualTo(GroupMemberStatus.ACTIVE);
    }

    @Test
    void 신청_취소가_거절_이력을_지우지_않는다() {
        Group group = group();
        GroupMember applicant = GroupMember.create(group, user("member@example.com"),
                GroupMemberStatus.PENDING, GroupMemberRole.MEMBER);
        applicant.rejectByLeader("한 번 거절됨");
        applicant.pending(); // 재신청
        when(groupMemberRepository.findByGroupIdAndUserUserId(GROUP_ID, MEMBER_ID)).thenReturn(applicant);

        groupService.cancelApplication(GROUP_ID, MEMBER_ID);

        // row를 지우면 rejectedCount가 사라져 3회 제한이 취소 한 번으로 초기화된다
        verify(groupMemberRepository, never()).delete(any(GroupMember.class));
        assertThat(applicant.getRejectedCount()).isEqualTo(1);
    }

    @Test
    void 거절된_적_없는_신청_취소는_흔적을_남기지_않는다() {
        Group group = group();
        GroupMember applicant = GroupMember.create(group, user("member@example.com"),
                GroupMemberStatus.PENDING, GroupMemberRole.MEMBER);
        when(groupMemberRepository.findByGroupIdAndUserUserId(GROUP_ID, MEMBER_ID)).thenReturn(applicant);

        groupService.cancelApplication(GROUP_ID, MEMBER_ID);

        verify(groupMemberRepository).delete(applicant);
    }

    @Test
    void 재신청하면_지난_거절_사유가_남지_않는다() {
        Group group = group();
        GroupMember applicant = GroupMember.create(group, user("member@example.com"),
                GroupMemberStatus.PENDING, GroupMemberRole.MEMBER);
        applicant.rejectByLeader("지난번 거절 사유");

        applicant.pending();

        assertThat(applicant.getMemberRejectReason()).isNull();
        assertThat(applicant.getRejectedCount()).isEqualTo(1);
    }
}
