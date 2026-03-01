package com.example.cbumanage.service;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.exception.CustomException;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.Study;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.repository.GroupMemberRepository;
import com.example.cbumanage.repository.PostRepository;
import com.example.cbumanage.repository.StudyRepository;
import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.utils.PostMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostService postService;
    private final GroupService groupService;
    private final GroupMemberRepository groupMemberRepository;

    @Autowired
    public StudyService(StudyRepository studyRepository,
                        PostRepository postRepository,
                        PostMapper postMapper,
                        PostService postService,
                        GroupService groupService,
                        GroupMemberRepository groupMemberRepository) {
        this.studyRepository = studyRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.postService = postService;
        this.groupService = groupService;
        this.groupMemberRepository = groupMemberRepository;
    }

    public Study createStudy(PostDTO.StudyCreateDTO req, Group group) {
        Post post = postRepository.findById(req.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        List<String> tags = (req.getStudyTags() != null) ? req.getStudyTags() : new ArrayList<>();
        Study study = Study.create(post, tags, req.getStudyName(), req.getMaxMembers(), req.isRecruiting(), group);
        return studyRepository.save(study);
    }

    // 스터디 상세 조회
    @Transactional
    public PostDTO.StudyInfoDetailDTO getStudyByPostId(Long postId, Long userId) {
        Study study = getActiveStudy(postId);
        boolean isLeader = userId != null && study.getPost().getAuthorId().equals(userId);
        Boolean hasApplied = groupService.hasAppliedToGroup(
                study.getGroup() != null ? study.getGroup().getId() : null, userId);
        return postMapper.toStudyInfoDetailDTO(study, isLeader, hasApplied);
    }

    // 카테고리별 목록 조회 (삭제 게시글 제외)
    @Transactional
    public Page<PostDTO.StudyListDTO> getPostsByCategory(Pageable pageable, int category) {
        Page<Study> studies = studyRepository.findByPostCategoryAndPostIsDeletedFalse(category, pageable);
        return studies.map(study -> postMapper.toStudyListDTO(study));
    }

    @Transactional
    public Page<PostDTO.StudyListDTO> getMyStudiesByUserId(Pageable pageable, Long userId, int category) {
        Page<Study> studies = studyRepository.findByPostAuthorIdAndPostCategoryAndPostIsDeletedFalse(userId, category, pageable);
        return studies.map(study -> postMapper.toStudyListDTO(study));
    }

    // Study 고유 필드 수정 (태그, 스터디명, 최대인원). updatePostStudy()에서 호출.
    public void updateStudy(PostDTO.StudyUpdateDTO dto, Study study) {
        if (dto.getStudyTags() != null) {
            study.updateStudyTags(dto.getStudyTags());
        }

        // 마감 후에는 studyName, maxMembers 변경 불가
        if (!study.isRecruiting() && (dto.getStudyName() != null || dto.getMaxMembers() != null)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "모집이 마감된 스터디의 이름과 최대 인원은 수정할 수 없습니다.");
        }

        if (dto.getStudyName() != null) {
            study.changeStudyName(dto.getStudyName());
        }

        if (dto.getMaxMembers() != null) {
            long activeCount = groupMemberRepository
                    .findByGroupIdAndGroupMemberStatus(study.getGroup().getId(), GroupMemberStatus.ACTIVE)
                    .size();
            if (dto.getMaxMembers() <= activeCount) {
                throw new CustomException(ErrorCode.INVALID_REQUEST,
                        "이미 수락된 인원(" + activeCount + "명)보다 작거나 같은 값으로 변경할 수 없습니다.");
            }
            study.changeMaxMembers(dto.getMaxMembers());
            groupService.updateGroupMaxMember(study.getGroup().getId(), dto.getMaxMembers());
        }
    }

    // Post + Study 한번에 생성 (게시글 생성 시 즉시 Group 생성)
    @Transactional
    public PostDTO.PostStudyCreateResponseDTO createPostStudy(PostDTO.PostStudyCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req, userId);
        Post post = postService.createPost(postCreateDTO);
        String groupName = req.getStudyName() + " #" + post.getId();
        Group group = groupService.createGroup(groupName, userId, req.getMaxMembers());
        PostDTO.StudyCreateDTO studyCreateDTO = postMapper.toStudyCreateDTO(req, post.getId());
        Study study = createStudy(studyCreateDTO, group);
        return postMapper.toPostStudyCreateResponseDTO(post, study, group);
    }

    // Post + Study 한번에 수정. recruiting은 여기서 변경할수 없고, 마감 API로만 변경 가능.
    @Transactional
    public void updatePostStudy(PostDTO.PostStudyUpdateRequestDTO req, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."));
        if (post.isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 게시글입니다.");
        }
        validateStudyOwner(post, userId);
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        postService.updatePost(postUpdateDTO, post);
        Study study = studyRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        PostDTO.StudyUpdateDTO studyUpdateDTO = postMapper.toStudyUpdateDTO(req);
        updateStudy(studyUpdateDTO, study);
    }

    // 소프트 삭제 (게시글 + 연결된 그룹 함께 삭제)
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
        if (study.getGroup() != null) {
            study.getGroup().delete();
        }
    }

    // 태그 정확히 일치 검색
    @Transactional
    public Page<PostDTO.StudyListDTO> searchByTag(String tag, Pageable pageable) {
        Page<Study> studies = studyRepository.findByExactTagAndPostIsDeletedFalse(tag, pageable);
        return studies.map(study -> postMapper.toStudyListDTO(study));
    }

    /**
     * 모집 마감.
     * Group은 게시글 생성 시 이미 만들어져 있으므로, 여기서는 모집 종료 처리만 수행.
     * 마감 → PENDING 일괄 거절 → Group 모집 CLOSED → Study recruiting false.
     */
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

        // 최소 1명 이상 수락 확인 (팀장=ACTIVE 1명 이미 포함, 추가 ACTIVE 필요)
        Long groupId = study.getGroup().getId();
        int activeCount = groupMemberRepository
                .findByGroupIdAndGroupMemberStatus(groupId, GroupMemberStatus.ACTIVE).size();
        if (activeCount <= 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "최소 1명 이상의 수락된 멤버가 있어야 합니다.");
        }

        // PENDING 일괄 거절
        groupService.rejectAllPendingMembers(groupId);

        // 그룹 모집 마감
        groupService.updateGroupRecruitment(groupId, userId, GroupRecruitmentStatus.CLOSED);

        // 스터디 모집 마감
        study.updateRecruiting(false);
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

}
