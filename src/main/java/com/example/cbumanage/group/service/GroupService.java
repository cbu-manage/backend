package com.example.cbumanage.group.service;

import com.example.cbumanage.group.dto.GroupDTO;
import com.example.cbumanage.global.error.CustomException;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.group.entity.GroupMember;
import com.example.cbumanage.group.entity.enums.GroupMemberRole;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.group.entity.enums.GroupApprovalAction;
import com.example.cbumanage.group.entity.enums.GroupStatus;
import com.example.cbumanage.group.entity.enums.GroupRecruitmentStatus;
import com.example.cbumanage.member.entity.enums.Role;
import com.example.cbumanage.group.repository.GroupRepository;
import com.example.cbumanage.group.repository.GroupMemberRepository;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.project.repository.ProjectRepository;
import com.example.cbumanage.study.repository.StudyRepository;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.group.util.GroupUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final CommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final StudyRepository studyRepository;
    private final GroupUtil groupUtil;

    /**
     게시글 생성 시 자동 생성되는 그룹.
     groupName=게시글 제목, 최소1명(고정) ·최대 N명(입력받은 값), 생성자를 리더로 추가하고 모집을 OPEN으로 설정.
     postId=연결된 게시글 ID (목록에서 게시글 본문 이동용).
     **/
    @Transactional
    public Group createGroup(String groupName, Long leaderId, int maxMember, Long postId, int category){
        Group group = Group.create(groupName, 1, maxMember, postId, category);
        groupRepository.save(group);
        CbuMember member = cbuMemberRepository.findById(leaderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"유저 정보를 찾을 수 없습니다."));
        GroupMember leader = GroupMember.create(group, member, GroupMemberStatus.ACTIVE, GroupMemberRole.LEADER);
        group.addMember(leader);
        groupMemberRepository.save(leader);
        group.openRecruitment();
        return group;
    }
    /*
    유저가 group에 가입신청을 하여 Group의 멤버를 추가하는 메소드 입니다.
    팀장이 아닌 멤버들은 기본적으로 Status로 Pending, Role 로 Member를 가집니다
     */
    @Transactional
    public GroupDTO.GroupMemberInfoDTO addGroupMember(Long groupId,
                                                      Long memberId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"그룹 정보를 찾을 수 없습니다."));


        // 모집 종료(CLOSED) 상태에서는 신청 불가
        if (group.getRecruitmentStatus() != GroupRecruitmentStatus.OPEN) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "모집중인 그룹이 아닙니다.");
        }

        GroupMember existing = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, memberId);
        if (existing != null) {
            if (existing.getGroupMemberStatus() == GroupMemberStatus.REJECTED) {
                existing.pending();
                return groupUtil.toGroupMemberInfoDTO(existing);
            }
            throw new CustomException(ErrorCode.ALREADY_JOINED_MEMBER,"중복 신청이 불가합니다.");
        }
        CbuMember member = cbuMemberRepository.findById(memberId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"유저 정보를 찾을 수 없습니다."));
        GroupMember groupMember = GroupMember.create(group,member,GroupMemberStatus.PENDING,GroupMemberRole.MEMBER);
        group.addMember(groupMember);
        groupMemberRepository.save(groupMember);
        return groupUtil.toGroupMemberInfoDTO(groupMember);
    }

    /**
    그룹을 id로 찾아오는 기능입니다
    그룹 상세보기에 사용되는 메서드 입니다.
    **/
    @Transactional(readOnly=true)
    public GroupDTO.GroupInfoDTO getGroupById(Long groupId){
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"해당 그룹을 찾을 수 없습니다."));
        return groupUtil.toGroupInfoDTO(group);
    }

    //그룹 모집 상태 변경. OPEN/CLOSED 시 연결된 Project 또는 Study의 recruiting도 동기화(저장)
    @Transactional
    public void updateGroupRecruitment(Long groupId, Long userId, GroupRecruitmentStatus targetStatus) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"해당 그룹을 찾을 수 없습니다."));
        assertIsGroupLeader(groupId, userId);
        boolean recruiting = (targetStatus == GroupRecruitmentStatus.OPEN);
        if (recruiting) {
            group.openRecruitment();
        } else {
            // 모집 마감 시 대기(PENDING) 신청자는 전원 거절(REJECTED) 처리
            rejectAllPendingMembers(groupId);
            // 관리자가 반려(REJECTED)했던 그룹은 리더가 모집 마감(CLOSED)을 다시 누르면 재요청(RESUBMITTED)로 전환
            if (group.getStatus() == GroupStatus.REJECTED) {
                group.resubmit();
            }
            group.closeRecruitment();
        }
        projectRepository.findByGroupId(groupId).ifPresent(project -> project.updateRecruiting(recruiting));
        studyRepository.findByGroupId(groupId).ifPresent(study -> study.updateRecruiting(recruiting));
    }

    /**
    멤버를 ACTIVE 또는 INACTIVE,REJECTED
    최대인원을 초과하면 그룹을 CLOSE 상태로 변경시키고 게시글의 모집중을 모집완료로 변경합니다.
     **/
    @Transactional
    public void updateStatusGroupMember(Long groupMemberId,Long userId,GroupMemberStatus targetStatus, String reason) {
        GroupMember groupMember = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 멤버를 찾을 수 없습니다."));
        Group group = groupMember.getGroup();
        assertIsGroupLeader(group.getId(), userId);
        if (targetStatus == GroupMemberStatus.ACTIVE) {
            groupMember.active();
            int activeCount = groupRepository.countByGroupIdAndStatus(group.getId(), GroupMemberStatus.ACTIVE);
            if (activeCount >= group.getMaxActiveMembers()) {
                group.closeRecruitment();
                projectRepository.findByGroupId(group.getId()).ifPresent(project -> project.updateRecruiting(false));
                studyRepository.findByGroupId(group.getId()).ifPresent(study -> study.updateRecruiting(false));
            }
        }else{
            groupMember.reject(reason);
        }
    }

    //시전한 user가 리더가 맞는지 확인하는 메소드
    private void assertIsGroupLeader(Long groupId, Long userId){
        GroupMember groupMember = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, userId);
        if(groupMember == null || groupMember.getGroupMemberRole() != GroupMemberRole.LEADER){
            throw new CustomException(ErrorCode.FORBIDDEN,"해당 그룹에 리더가 아닙니다.");
        }
    }

    //시전한 user가 관리자가 맞는지 확인하는 메서드
    private void assertIsAdmin(Long userId){
        CbuMember cbuMember = cbuMemberRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"유저 정보를 찾을 수 없습니다."));
        boolean isAdmin = cbuMember.getRole().stream()
                .anyMatch(role -> role==Role.ADMIN);
        if (!isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN,"관리자가 아닙니다");
        }
    }

    /* PENDING 상태인 모든 멤버를 일괄 거절 (모집 마감 시 사용) */
    @Transactional
    public void rejectAllPendingMembers(Long groupId) {
        String reason="모집이 마감되어 자동으로 거절되었습니다.";
        List<GroupMember> pending = groupMemberRepository.findByGroupIdAndGroupMemberStatus(groupId, GroupMemberStatus.PENDING);
        pending.forEach(member -> member.reject(reason));
    }


    /* 신청 취소: PENDING 상태인 본인 신청만 삭제 */
    @Transactional
    public void cancelApplication(Long groupId, Long userId) {
        GroupMember gm = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, userId);
        if (gm == null || gm.getGroupMemberStatus() != GroupMemberStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"PENDING 상태가 아닙니다.");
        }
        groupMemberRepository.delete(gm);
    }

    /* 팀장 전용: PENDING 신청인원 목록 조회 메서드(학년, 학부, 이름) */
    @Transactional(readOnly = true)
    public List<GroupDTO.GroupMemberInfoDTO> getPendingGroupMember(Long groupId, Long userId) {
        assertIsGroupLeader(groupId, userId);
        List<GroupMember> pending = groupMemberRepository.findByGroupIdAndGroupMemberStatus(groupId, GroupMemberStatus.PENDING);
        return pending.stream().map(groupUtil::toGroupMemberInfoDTO).toList();
    }

    //팀장 전용: 신청인원 전체 상태 한눈에 확인(PENDING/ACTIVE/REJECTED/INACTIVE 팀원 목록)
    @Transactional(readOnly = true)
    public List<GroupDTO.GroupMemberInfoDTO> getGroupApplicantsOverview(Long groupId, Long userId) {
        assertIsGroupLeader(groupId, userId);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .filter(m -> m.getGroupMemberRole() != GroupMemberRole.LEADER)
                .map(groupUtil::toGroupMemberInfoDTO)
                .toList();
    }

    /* 해당 그룹에 해당 유저가 PENDING(신청 대기) 상태로 있는지 여부 */
    @Transactional(readOnly = true)
    public Boolean hasAppliedToGroup(Long groupId, Long userId) {
        if (userId == null) return false; // 비로그인 = 신청 이력 없음
        if (groupId == null) return null;
        GroupMember gm = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, userId);
        if (gm == null) return false; // 미가입자
        GroupMemberStatus status = gm.getGroupMemberStatus();
        if (status == GroupMemberStatus.PENDING) return true;   // 신청 대기
        if (status == GroupMemberStatus.REJECTED) return false; // 거절 → 재신청 가능
        return null; // ACTIVE, INACTIVE → 가입 완료
    }

    /*관리자 전용: 그룹 승인 상태를 ACTIVE 또는 REJECTED로 변경합니다.*/
    @Transactional
    public void updateGroupStatusAdmin(Long groupId, Long adminId, GroupDTO.GroupReviewRequestDTO req) {
//        assertIsAdmin(adminId);
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 그룹을 찾을 수 없습니다."));
        if (req.action() == GroupApprovalAction.APPROVE) {
            group.approve();
        } else {
            group.reject(req.reason()); //그룹 거절 사유 추가
            group.openRecruitment(); //그룹 모집 상태 OPEN
            projectRepository.findByGroupId(groupId)
                    .ifPresent(project -> project.updateRecruiting(true));
            studyRepository.findByGroupId(groupId)
                    .ifPresent(study -> study.updateRecruiting(true));
        }
    }

    //최대 모집 인원을 변경합니다.
    @Transactional
    public void updateGroupMaxMember(Long groupId, int maxMember){
        Group group =  groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"그룹을 찾을 수 없습니다."));
        int activeCount = groupRepository.countByGroupIdAndStatus(group.getId(), GroupMemberStatus.ACTIVE);
        if (activeCount >= maxMember) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "현재 참여 인원(" + activeCount + "명)보다 적은 인원으로 수정할 수 없습니다.");
        }
        group.changeMaxActiveMembers(maxMember);
    }

    //개설되어 있는 그룹 전체를 조회하는 기능입니다. (관리자 전용)
    @Transactional(readOnly = true)
    public Page<GroupDTO.GroupListDTO> getAllGroups(Long userId, GroupStatus groupStatus, Pageable pageable) {
//        assertIsAdmin(userId);
        Page<Group> groups = groupRepository.findByGroupStatus(groupStatus, GroupRecruitmentStatus.CLOSED, pageable);
        return groups.map(groupUtil::toGroupListDTO);
    }

    //자신이 속한 그룹들을 조회하기 위한 메서드 입니다.
    @Transactional(readOnly = true)
    public List<GroupDTO.GroupListDTO> getJoinedGroups(Long userId){
        List<Group> groups = groupRepository.findByUserId(userId,GroupMemberStatus.ACTIVE);
        return groups.stream().map(group -> groupUtil.toGroupListDTO(group)).toList();
    }

    //본인이 신청한 그룹 목록 카테고리별 페이징 (승인/대기/거절/비활동). 리더로 소속된 그룹은 제외, 삭제된 그룹 제외
    @Transactional(readOnly = true)
    public Page<GroupDTO.MyGroupApplicationListDTO> getMyAppliedGroupsByCategory(Long userId, Integer category, Pageable pageable) {
        Page<GroupMember> members = groupMemberRepository.findMyApplicationsByCategory(
                userId,
                category,
                GroupMemberRole.LEADER,
                pageable
        );
        return members.map(groupUtil::toMyGroupApplicationListDTO);
    }

    //그룹을 이름으로 검색하는 기능입니다.
    @Transactional(readOnly = true)
    public List<GroupDTO.GroupInfoDTO> getGroupByGroupNameAndStatus(String groupName ) {
        List<Group> groups = groupRepository.findByGroupNameContaining(groupName);
        return groups.stream().map(group -> groupUtil.toGroupInfoDTO(group)).toList();
    }
}
