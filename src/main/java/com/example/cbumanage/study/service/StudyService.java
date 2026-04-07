package com.example.cbumanage.study.service;

import com.example.cbumanage.group.service.GroupService;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.global.error.CustomException;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.study.entity.Study;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.group.entity.enums.GroupRecruitmentStatus;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.group.repository.GroupRepository;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.study.repository.StudyRepository;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.util.PostMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final PostRepository postRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final PostService postService;
    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostDTO.PostStudyCreateResponseDTO createPostStudy(PostDTO.PostStudyCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = new PostDTO.PostCreateDTO(userId, req.title(), req.content(), PostCategory.STUDY.getValue());
        Post post = postService.createPost(postCreateDTO);

        Group group = groupService.createGroup(req.studyName(), userId, req.maxMembers(), post.getId(), post.getCategory());

        List<String> tags = (req.studyTags() != null) ? req.studyTags() : List.of();
        Study study = Study.create(post, tags, req.studyName(), group);
        studyRepository.save(study);

        CbuMember author = cbuMemberRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자를 찾을 수 없습니다."));

        return postMapper.toPostStudyCreateResponseDTO(post, study, group, author);
    }

    @Transactional
    public PostDTO.StudyInfoDetailDTO getStudyByPostId(Long postId, Long userId) {
        Study study = getActiveStudy(postId);

        postRepository.incrementViewCount(postId);

        boolean isLeader = userId != null && study.getPost().getAuthorId().equals(userId);
        Boolean hasApplied = groupService.hasAppliedToGroup(study.getGroup().getId(), userId);

        CbuMember author = cbuMemberRepository.findById(study.getPost().getAuthorId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "작성자를 찾을 수 없습니다."));

        int active = groupRepository.countByGroupIdAndStatus(study.getGroup().getId(), GroupMemberStatus.ACTIVE);
        int max = study.getGroup().getMaxActiveMembers();

        return postMapper.toStudyInfoDetailDTO(study, isLeader, hasApplied, author, active, max,
                study.getPost().getViewCount() + 1);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO.StudyListDTO> getPostsByCategory(Pageable pageable, int category) {
        Page<Study> studies = studyRepository.findByPostCategoryAndPostIsDeletedFalse(category, pageable);
        return mapStudiesToStudyListDTO(studies);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO.StudyListDTO> getMyStudiesByUserId(Pageable pageable, Long userId, int category) {
        Page<Study> studies = studyRepository.findByPostAuthorIdAndPostCategoryAndPostIsDeletedFalse(userId, category, pageable);
        return mapStudiesToStudyListDTO(studies);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO.StudyListDTO> searchByTag(String tag, Pageable pageable) {
        Page<Study> studies = studyRepository.findByExactTagAndPostIsDeletedFalse(tag, pageable);
        return mapStudiesToStudyListDTO(studies);
    }

    @Transactional
    public void updatePostStudy(PostDTO.PostStudyUpdateRequestDTO req, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."));

        if (post.isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 게시글입니다.");
        }

        validateStudyOwner(post, userId);

        postService.updatePost(new PostDTO.PostUpdateDTO(req.title(), req.content()), post);

        Study study = studyRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));

        if (req.studyName() != null) {
            study.changeStudyName(req.studyName());
            study.getGroup().changeGroupName(req.studyName());
        }

        if (req.studyTags() != null) {
            study.updateStudyTags(req.studyTags());
        }

        if (req.maxMembers() != null) {
            if (!study.isRecruiting()) {
                throw new CustomException(ErrorCode.INVALID_REQUEST,
                        "모집이 마감된 스터디의 최대 인원은 수정할 수 없습니다.");
            }
            groupService.updateGroupMaxMember(study.getGroup().getId(), req.maxMembers());
        }
    }

    @Transactional
    public void softDeletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."));

        if (post.isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "이미 삭제된 게시글입니다.");
        }

        validateStudyOwner(post, userId);

        post.delete();

        Study study = studyRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));

        study.getGroup().delete();
    }

    @Transactional
    public void closeStudyRecruitment(Long postId, Long userId) {
        Study study = studyRepository.findByPostIdForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));

        if (study.getPost().isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 게시글입니다.");
        }

        validateStudyOwner(study.getPost(), userId);

        if (!study.isRecruiting()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 모집이 마감된 스터디입니다.");
        }

        Long groupId = study.getGroup().getId();
        int activeCount = groupRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);

        if (activeCount <= 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "최소 1명 이상의 수락된 멤버가 있어야 합니다.");
        }

        groupService.updateGroupRecruitment(groupId, userId, GroupRecruitmentStatus.CLOSED);
    }

    private Study getActiveStudy(Long postId) {
        Study study = studyRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));

        if (study.getPost().isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 게시글입니다.");
        }

        return study;
    }

    private void validateStudyOwner(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "게시글에 대한 권한이 없습니다.");
        }
    }

    private Page<PostDTO.StudyListDTO> mapStudiesToStudyListDTO(Page<Study> studies) {
        if (studies.getContent().isEmpty()) {
            return studies.map(s -> postMapper.toStudyListDTO(s, null, 0, s.getGroup().getMaxActiveMembers()));
        }

        Set<Long> authorIds = studies.getContent().stream()
                .map(s -> s.getPost().getAuthorId())
                .collect(Collectors.toSet());

        Map<Long, CbuMember> authorMap = cbuMemberRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(CbuMember::getCbuMemberId, m -> m));

        return studies.map(s -> {
            int active = groupRepository.countByGroupIdAndStatus(s.getGroup().getId(), GroupMemberStatus.ACTIVE);
            int max = s.getGroup().getMaxActiveMembers();
            return postMapper.toStudyListDTO(s, authorMap.get(s.getPost().getAuthorId()), active, max);
        });
    }
}
