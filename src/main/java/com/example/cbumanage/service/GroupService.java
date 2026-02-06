package com.example.cbumanage.service;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.GroupMember;
import com.example.cbumanage.model.enums.*;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.CommentRepository;
import com.example.cbumanage.repository.GroupMemberRepository;
import com.example.cbumanage.repository.GroupRepository;
import com.example.cbumanage.utils.GroupUtil;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final CommentRepository commentRepository;
    private final GroupUtil groupUtil;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        CbuMemberRepository cbuMemberRepository,
                        CommentRepository commentRepository,
                        GroupUtil groupUtil) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.commentRepository = commentRepository;
        this.groupUtil = groupUtil;
    }

    /*
    유저가 group에 가입신청을 하여
    Group의 멤버를 추가하는 메소드 입니다. 팀장이 아닌 멤버들은 기본적으로
    Status로 Pending, Role 로 Member를 가집니다
     */
    @Transactional
    public GroupDTO.GroupMemberInfoDTO addGroupMember(Long groupId,
                                                      Long memberId) {
        Group group = groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        CbuMember member = cbuMemberRepository.findById(memberId).orElseThrow(()-> new EntityNotFoundException("Member not found"));
        boolean isActiveMember =
                groupMemberRepository.existsActiveMember(
                        memberId,
                        groupId,
                        GroupMemberStatus.ACTIVE
                );
        if (isActiveMember){
            throw new EntityExistsException("이미 가입된 멤버입니다");
        }

        GroupMember groupMember = GroupMember.create(group,member,GroupMemberStatus.PENDING,GroupMemberRole.MEMBER);
        group.addMember(groupMember);
        groupMemberRepository.save(groupMember);

        return groupUtil.toGroupMemberInfoDTO(groupMember);
    }

    /*
    그룹을 생성하는 메소드입니다,
    그룹의 생성과 동시에 그룹을 생성한 유저를 Leader 역할과 ACTIVE상태를 부여하여 멤버로 추가합니다
     */
    @Transactional
    public GroupDTO.GroupCreateResponseDTO createGroup(GroupDTO.GroupCreateRequestDTO req,
                                                       Long leaderId){
        Group group = Group.create(req.getGroupName(), req.getMinActiveMembers(), req.getMaxActiveMembers());
        groupRepository.save(group);
        CbuMember member = cbuMemberRepository.findById(leaderId).orElseThrow(()-> new EntityNotFoundException("Member not found"));

        GroupMember leader = GroupMember.create(group,member,GroupMemberStatus.ACTIVE,GroupMemberRole.LEADER);
        group.addMember(leader);
        groupMemberRepository.save(leader);
        return groupUtil.toGroupCreateResponseDTO(group);

    }

    //그룹을 id로 찾아오는 기능입니다
    //포스트목록을 불러올때 연결되어있는 그룹의 정보를 불러올때 사용합니다
    public GroupDTO.GroupInfoDTO getGroupById(Long groupId){
        Group group = groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        return groupUtil.toGroupInfoDTO(group);
    }

    //그룹을 검색하는 기능입니다.
    public List<GroupDTO.GroupInfoDTO> getGroupByGroupNameAndStatus(String groupName ) {
        List<Group> groups = groupRepository.findByGroupNameContaining(groupName);
        return groups.stream().map(group -> groupUtil.toGroupInfoDTO(group)).toList();
    }

    /*

     */
    public List<GroupDTO.GroupInfoDTO> getGroupByMemberId(Long userId){
        List<Group> groups = groupRepository.findByUserId(userId);
        return groups.stream().map(group -> groupUtil.toGroupInfoDTO(group)).toList();
    }
    //그룹의 모집을 시작시키는 메소드입니다
    @Transactional
    public void openGroupRecruitment(Long groupId,Long userId){
        Group group = groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        assertIsGroupLeader(groupId,userId);
        group.openRecruitment();
    }

    //그룹의 모집을 종료시키는 메소드입니다
    @Transactional
    public void closeGroupRecruitment(Long groupId, Long userId){
        Group group = groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        assertIsGroupLeader(groupId,userId);
        group.closeRecruitment();
    }

    //그룹을 활동상태로 변경시키는 메소드 입니다
    @Transactional
    public void activateGroupStatus(Long groupId,Long userId){
        Group group = groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        assertIsAdmin(userId);
        group.activate();
    }

    //그룹을 비활동 상태로 변경시키는 메소드 입니다
    @Transactional
    public void deactivateGroupStatus(Long groupId,Long userId){
        Group group = groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        assertIsAdmin(userId);
        group.deactivate();
    }

    //멤버를 Active상태로 변경시키는 메소드 입니다(팀장이 가입 신청한 멤버를 활동 멤버로 변경시킬때 사용합니다)
    @Transactional
    public void activateGroupMember(Long groupMemberId,Long userId){
        GroupMember groupMember = groupMemberRepository.findById(groupMemberId).orElseThrow(()-> new EntityNotFoundException("Member not found"));
        Group group = groupMember.getGroup();
        assertIsGroupLeader(group.getId(),userId);
        groupMember.changeStatus(GroupMemberStatus.ACTIVE);


    }

    @Transactional
    public void deactivateGroupMember(Long groupMemberId,Long userId){
        GroupMember groupMember = groupMemberRepository.findById(groupMemberId).orElseThrow(()-> new EntityNotFoundException("Member not found"));
        Group group = groupMember.getGroup();
        assertIsGroupLeader(group.getId(),userId);
        groupMember.changeStatus(GroupMemberStatus.INACTIVE);
    }

    @Transactional
    public void updateGroup(Long groupId, GroupDTO.GroupUpdateRequestDTO req){
        Group group =  groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        group.changeGroupName(req.getGroupName());
        group.changeMinActiveMembers(req.getMinActiveMembers());
        group.changeMaxActiveMembers(req.getMaxActiveMembers());
    }

    //시전한 user가 리더가 맞는지 확인하는 메소드 - 변수정리
    private void assertIsGroupLeader(Long groupId, Long userId){
        Group g = groupRepository.findById(groupId).orElseThrow(()-> new EntityNotFoundException("Group not found"));
        CbuMember cbuMember =  cbuMemberRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found"));
        GroupMember groupMember = groupMemberRepository.findByGroupIdAndCbuMemberCbuMemberId(groupId, userId);
        if(!groupMember.getGroupMemberRole().equals(GroupMemberRole.LEADER)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void assertIsAdmin(Long userId){
        CbuMember cbuMember = cbuMemberRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("User not found"));
        if(!cbuMember.getRole().equals(Role.ADMIN)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }



}
