package com.example.cbumanage.report.service;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.report.entity.PostReport;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.report.repository.PostReportRepository;
import com.example.cbumanage.group.repository.GroupMemberRepository;
import com.example.cbumanage.post.util.PostMapper;
import com.example.cbumanage.reportmember.entity.ReportMember;
import com.example.cbumanage.reportmember.repository.ReportMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostReportService {

    private final PostService postService;
    private final PostRepository postRepository;
    private final PostReportRepository postReportRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ReportMemberRepository reportMemberRepository;

    public PostReport createReport(PostDTO.ReportCreateDTO req) {
        Post post = postRepository.findById(req.postId()).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostReport report = PostReport.create(post, req.groupId(), req.date(), req.location(), req.reportImage(), req.reflection(), req.nextPlan());
        PostReport saved = postReportRepository.save(report);
        return saved;
    }

    @Transactional
    public PostDTO.PostReportCreateResponseDTO createPostReport(PostDTO.PostReportCreateRequestDTO req,Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req,userId);
        Post post = postService.createPost(postCreateDTO);
        PostDTO.ReportCreateDTO reportCreateDTO = postMapper.toReportCreateDTO(req, post.getId());
        PostReport report = createReport(reportCreateDTO);
        saveReportMembers(report.getId(), req.groupId(), req.memberIds());
        return postMapper.toPostReportCreateResponseDTO(post, report);
    }

    private void saveReportMembers(Long reportId, Long groupId, List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return;
        validateReportMembers(groupId, memberIds);

        memberIds.stream()
                .distinct()
                .map(memberId -> ReportMember.create(reportId, memberId))
                .forEach(reportMemberRepository::save);
    }

    private void validateReportMembers(Long groupId, List<Long> memberIds) {
        Set<Long> invalidMemberIds = new LinkedHashSet<>();

        for (Long memberId : memberIds) {
            boolean isActiveGroupMember = groupMemberRepository.existsByGroupIdAndUserUserIdAndGroupMemberStatus(
                    groupId,
                    memberId,
                    GroupMemberStatus.ACTIVE
            );
            if (!isActiveGroupMember) {
                invalidMemberIds.add(memberId);
            }
        }

        if (!invalidMemberIds.isEmpty()) {
            throw new BaseException(
                    ErrorCode.REPORT_MEMBER_NOT_IN_GROUP,
                    "그룹에 속하지 않은 참여 멤버가 포함되어 있습니다. groupId=" + groupId + ", memberIds=" + invalidMemberIds
            );
        }
    }

    /*
보고서 게시글 미리보기 리스트 입니다. 테스트를위해 카테고리는 7로 자동주입해서 사용합니다

fetch join -> 해결
 */
    public Page<PostDTO.PostReportPreviewDTO> getPostReportPreviewDTOList(Pageable pageable, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        boolean isAdmin = user.getRole().canViewAllReports();

        if (isAdmin) {
            return postReportRepository.findPostReportPreviews(pageable, 7);
        }

        List<Long> groupIds = groupMemberRepository.findGroupIdsByUserIdAndStatus(userId, GroupMemberStatus.ACTIVE);
        if (groupIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return postReportRepository.findPostReportPreviewsByGroupIds(pageable, 7, groupIds);
    }

    public Page<PostDTO.PostReportPreviewDTO> getGroupPostReportPreviewDTOList(Pageable pageable, Long groupId) {
        return postReportRepository.findPostReportPreviewsByGroupId(pageable, 7, groupId);
    }

    public Page<PostDTO.PostReportPreviewDTO> getMyPostReportPreviewDTOList(Pageable pageable,Long userId) {
        Page<PostDTO.PostReportPreviewDTO> myReportPreviewDTOS=postReportRepository.findMyPostReportPreviews(pageable,7,userId);
        return myReportPreviewDTOS;
    }

    /*
보고서 포스트 자세히 보기 메소드입니다. post와 report를 한번에 가져옵니다
 */
    public PostDTO.PostReportViewDTO getPostReportViewDTO(Long postId,Long userId){
        PostReport report = postReportRepository.findByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("Report Not Found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        boolean isAdmin = user.getRole().canViewAllReports();
        boolean isActiveMember =
                groupMemberRepository.existsActiveMember(
                        userId,
                        report.getGroupId(),
                        GroupMemberStatus.ACTIVE
                );

        if (!(isAdmin || isActiveMember)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return postMapper.toPostReportViewDTO(post, report);
    }

    /*
컨트롤러에서 요청을 받아 각 DTO 로 나누고 알맞는 메소드를 호출합니다
Create 와  마찬가지로 컨트롤러에서 부르는 메소드는 이 메소드이기에, 해당 메소드에 Transactional 를 추가했습니다
 */
    @Transactional
    public void updatePostReport(PostDTO.PostReportUpdateRequestDTO req,Long postId,Long userId) {
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        boolean canEditAll = user.getRole().canViewAllReports();
        if(!canEditAll && !post.getAuthorId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"NOT POST OWNER");
        }
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        postService.updatePost(postUpdateDTO,post);
        PostReport report = postReportRepository.findByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("Report Not Found"));
        PostDTO.ReportUpdateDTO reportUpdateDTO=postMapper.topostReportUpdateDTO(req);
        updateReport(reportUpdateDTO,report);
        reportMemberRepository.deleteByReportId(report.getId());
        saveReportMembers(report.getId(), report.getGroupId(), req.memberIds());
    }

    @Transactional
    public void acceptReport(Long postId,Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        if (!user.getRole().isPresidentOrVicePresidentOrAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        PostReport report = postReportRepository.findByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("Report Not Found"));
        report.Accept();
    }

    public void updateReport(PostDTO.ReportUpdateDTO postUpdateDTO,PostReport postReport) {
        postReport.changeDate(postUpdateDTO.date());
        postReport.changeLocation(postUpdateDTO.location());
        postReport.changeReportImage(postUpdateDTO.reportImage());
        postReport.changeReflection(postUpdateDTO.reflection());
        postReport.changeNextPlan(postUpdateDTO.nextPlan());
    }
}
