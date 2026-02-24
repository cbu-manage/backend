package com.example.cbumanage.service;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.exception.CustomException;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.GroupMember;
import com.example.cbumanage.model.enums.*;
import com.example.cbumanage.repository.*;
import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.utils.GroupUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final CommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final GroupUtil groupUtil;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        CbuMemberRepository cbuMemberRepository,
                        CommentRepository commentRepository,
                        ProjectRepository projectRepository,
                        GroupUtil groupUtil) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.commentRepository = commentRepository;
        this.projectRepository = projectRepository;
        this.groupUtil = groupUtil;
    }

    /**
     게시글 생성 시 자동 생성되는 그룹.
     groupName=게시글 제목, 최소1명·최대10명 고정, 생성자를 리더로 추가하고 모집을 OPEN으로 설정.
     **/
    @Transactional
    public Group createGroup(String groupName, Long leaderId){
        Group group = Group.create(groupName, 1, 10);
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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"그룹 정보를 찾을 수 없습니다."));
        GroupMember existing = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, memberId);
        if (existing != null) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_MEMBER);
        }
        CbuMember member = cbuMemberRepository.findById(memberId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"유저 정보를 찾을 수 없습니다."));
        GroupMember groupMember = GroupMember.create(group,member,GroupMemberStatus.PENDING,GroupMemberRole.MEMBER);
        group.addMember(groupMember);
        groupMemberRepository.save(groupMember);
        return groupUtil.toGroupMemberInfoDTO(groupMember);
    }

     // 서비스 내부 호출용 그룹 생성 메서드
     // DTO 없이 파라미터로 직접 값을 받아 그룹을 생성합니다.
    @Transactional
    public GroupDTO.GroupCreateResponseDTO createGroupInternal(String groupName,
                                                                int minActiveMembers,
                                                                int maxActiveMembers,
                                                                Long leaderId) {
        Group group = Group.create(groupName, minActiveMembers, maxActiveMembers);
        groupRepository.save(group);
        CbuMember member = cbuMemberRepository.findById(leaderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"유저 정보를 찾을 수 없습니다."));

        GroupMember leader = GroupMember.create(group, member, GroupMemberStatus.ACTIVE, GroupMemberRole.LEADER);
        group.addMember(leader);
        groupMemberRepository.save(leader);
        return groupUtil.toGroupCreateResponseDTO(group);
    }

    /**
    그룹을 id로 찾아오는 기능입니다
    그룹 상세보기에 사용되는 메서드 입니다.
    **/
    public GroupDTO.GroupInfoDTO getGroupById(Long groupId){
        Group group = groupRepository.findById(groupId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"해당 그룹을 찾을 수 없습니다."));
        return groupUtil.toGroupInfoDTO(group);
    }

    //그룹의 모집을 종료시키는 메소드입니다
    @Transactional
    public void updateGroupRecruitment(Long groupId, Long userId, GroupRecruitmentStatus targetStatus) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"해당 그룹을 찾을 수 없습니다."));
        assertIsGroupLeader(groupId,userId);
        if(targetStatus==GroupRecruitmentStatus.OPEN){
            group.openRecruitment();
        }else{
            group.closeRecruitment();
        }
    }

    /**
    멤버를 ACTIVE 또는 INACTIVE,REJECTED
    최대인원을 초과하면 그룹을 CLOSE 상태로 변경시키고 게시글의 모집중을 모집완료로 변경합니다.
     **/
    @Transactional
    public void updateStatusGroupMember(Long groupMemberId,Long userId,GroupMemberStatus targetStatus) {
        GroupMember groupMember = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 멤버를 찾을 수 없습니다."));
        Group group = groupMember.getGroup();
        assertIsGroupLeader(group.getId(), userId);
        groupMember.changeStatus(targetStatus);
        if (targetStatus == GroupMemberStatus.ACTIVE) {
            int activeCount = groupRepository.countByGroupIdAndStatus(group.getId(), GroupMemberStatus.ACTIVE);
            if (activeCount >= group.getMaxActiveMembers()) {
                group.closeRecruitment();
                projectRepository.findByGroupId(group.getId()).ifPresent(project -> {
                    project.updateRecruiting(false);
                });
            }
        }
    }

    //시전한 user가 리더가 맞는지 확인하는 메소드
    private void assertIsGroupLeader(Long groupId, Long userId){
        GroupMember groupMember = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, userId);
        if(groupMember.getGroupMemberRole()!=(GroupMemberRole.LEADER)){
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
    public List<GroupDTO.GroupMemberInfoDTO> getPendingGroupMember(Long groupId, Long userId) {
        assertIsGroupLeader(groupId, userId);
        List<GroupMember> pending = groupMemberRepository.findByGroupIdAndGroupMemberStatus(groupId, GroupMemberStatus.PENDING);
        return pending.stream().map(groupUtil::toGroupMemberInfoDTO).toList();
    }

    /* 해당 그룹에 해당 유저가 PENDING(신청 대기) 상태로 있는지 여부 */
    public Boolean hasAppliedToGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) return null;
        GroupMember gm = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, userId);
        if (gm == null) return false; // 미가입자
        GroupMemberStatus status = gm.getGroupMemberStatus();
        if (status == GroupMemberStatus.PENDING) return true;  // 펜딩자
        return null;
    }

    /*관리자 전용: 그룹 상태를 ACTIVE 또는 INACTIVE로 변경합니다.*/
    @Transactional
    public void updateGroupStatusAdmin(Long groupId, Long adminId, GroupStatus targetStatus) {
        assertIsAdmin(adminId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 그룹을 찾을 수 없습니다."));
        if (targetStatus == GroupStatus.ACTIVE) {
            group.activate();
        } else {
            group.deactivate();
        }
    }

    @Transactional
    public void updateGroup(Long groupId, GroupDTO.GroupUpdateRequestDTO req){
        Group group =  groupRepository.findById(groupId).orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND));
        group.changeGroupName(req.getGroupName());
        group.changeMinActiveMembers(req.getMinActiveMembers());
        group.changeMaxActiveMembers(req.getMaxActiveMembers());
    }

    //개설되어 있는 그룹 전체를 조회하는 기능입니다. (관리자 전용)
    public List<GroupDTO.GroupListDTO> getAllGroups(Long userId) {
        assertIsAdmin(userId);
        List<Group> groups = groupRepository.findAll();
        return groups.stream().map(group->groupUtil.toGroupListDTO(group)).toList();
    }

    //자신이 속한 그룹들을 조회하기 위한 메서드 입니다.
    public List<GroupDTO.GroupInfoDTO> getJoinedGroups(Long userId){
        List<Group> groups = groupRepository.findByUserId(userId);
        return groups.stream().map(group -> groupUtil.toGroupInfoDTO(group)).toList();
    }

    //그룹을 이름으로 검색하는 기능입니다.
    public List<GroupDTO.GroupInfoDTO> getGroupByGroupNameAndStatus(String groupName ) {
        List<Group> groups = groupRepository.findByGroupNameContaining(groupName);
        return groups.stream().map(group -> groupUtil.toGroupInfoDTO(group)).toList();
    }

    /**
     멤버를 Active 상태로 변경시키는 메소드 입니다(팀장이 가입 신청한 멤버를 수락할 때 사용되는 메서드)
     **/
    @Transactional
    public void activateGroupMember(Long groupMemberId,Long userId){
        GroupMember groupMember = groupMemberRepository.findById(groupMemberId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"해당 멤버를 찾을 수 없습니다."));
        Group group = groupMember.getGroup();
        assertIsGroupLeader(group.getId(),userId);
        groupMember.changeStatus(GroupMemberStatus.ACTIVE);
    }
}
