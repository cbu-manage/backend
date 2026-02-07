package com.example.cbumanage.service;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.dto.StudyApplyDTO;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.Study;
import com.example.cbumanage.model.StudyApply;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.StudyApplyStatus;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.PostRepository;
import com.example.cbumanage.repository.StudyApplyRepository;
import com.example.cbumanage.repository.StudyRepository;
import com.example.cbumanage.utils.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostService postService;
    private final StudyApplyRepository studyApplyRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final GroupService groupService;

    @Autowired
    public StudyService(StudyRepository studyRepository,
                        PostRepository postRepository,
                        PostMapper postMapper,
                        PostService postService,
                        StudyApplyRepository studyApplyRepository,
                        CbuMemberRepository cbuMemberRepository,
                        GroupService groupService) {
        this.studyRepository = studyRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.postService = postService;
        this.studyApplyRepository = studyApplyRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.groupService = groupService;
    }

    /**
     * 스터디 게시글 생성 (Study 서브 테이블만)
     */
    public Study createStudy(PostDTO.StudyCreateDTO req) {
        Post post = postRepository.findById(req.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        List<String> tags = (req.getStudyTags() != null) ? req.getStudyTags() : new ArrayList<>();
        Study study = Study.create(post, tags, req.isRecruiting());
        return studyRepository.save(study);
    }

    /**
     * 스터디 게시글 상세 조회
     */
    public PostDTO.StudyInfoDetailDTO getStudyByPostId(Long postId) {
        Study study = studyRepository.findByPostId(postId);
        return postMapper.toStudyInfoDetailDTO(study);
    }

    /**
     * 스터디 게시글 목록 페이징 조회 (카테고리 기준)
     */
    public Page<PostDTO.StudyListDTO> getPostsByCategory(Pageable pageable, int category) {
        Page<Study> studies = studyRepository.findByPostCategoryAndPostIsDeletedFalse(category, pageable);
        return studies.map(study -> postMapper.toStudyListDTO(study));
    }

    /**
     * 스터디 게시글 수정 (Study 필드만)
     */
    public void updateStudy(PostDTO.StudyUpdateDTO dto, Study study) {
        study.updateStudyTags(dto.getStudyTags());
        study.updateRecruiting(dto.isRecruiting());
    }

    /**
     * 스터디 게시글 생성 (Post + Study 트랜잭션)
     */
    @Transactional
    public PostDTO.PostStudyCreateResponseDTO createPostStudy(PostDTO.PostStudyCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req, userId);
        Post post = postService.createPost(postCreateDTO);
        PostDTO.StudyCreateDTO studyCreateDTO = postMapper.toStudyCreateDTO(req, post.getId());
        Study study = createStudy(studyCreateDTO);
        return postMapper.toPostStudyCreateResponseDTO(post, study);
    }

    /**
     * 스터디 게시글 수정 (Post + Study 트랜잭션)
     */
    @Transactional
    public void updatePostStudy(PostDTO.PostStudyUpdateRequestDTO req, Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        // 권한 확인
        validateStudyOwner(post, userId);
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        postService.updatePost(postUpdateDTO, post);
        Study study = studyRepository.findByPostId(postId);
        PostDTO.StudyUpdateDTO studyUpdateDTO = postMapper.toStudyUpdateDTO(req);
        updateStudy(studyUpdateDTO, study);
    }

    /**
     * 스터디 게시글 소프트 삭제
     */
    @Transactional
    public void softDeletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Study Not Found"));
        // 권한 확인
        validateStudyOwner(post, userId);
        post.delete();
    }

    /**
     * 스터디 태그별 목록 조회.
     * 사용자가 추가한 태그 문자열로 검색.
     */
    @Transactional
    public Page<PostDTO.StudyListDTO> searchByTag(String tag, Pageable pageable) {
        Page<Study> studies = studyRepository.findByStudyTagsContainingAndPostIsDeletedFalse(tag, pageable);
        return studies.map(study -> postMapper.toStudyListDTO(study));
    }

    /**
     * 유효 권한 확인 메서드
     */
    private void validateStudyOwner(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "게시글에 대한 권한이 없습니다."
            );
        }
    }

    /**
     * 스터디 신청
     */
    @Transactional
    public StudyApplyDTO.StudyApplyInfoDTO applyStudy(Long postId, Long applicantId) {
        Study study = studyRepository.findByPostId(postId);
        if (study == null) {
            throw new EntityNotFoundException("Study Not Found");
        }

        // 모집 중인지 확인
        if (!study.isRecruiting()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모집이 종료된 스터디입니다.");
        }

        // 중복 신청 확인
        if (studyApplyRepository.existsByStudyIdAndApplicantCbuMemberId(study.getId(), applicantId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신청한 스터디입니다.");
        }

        // 본인 스터디에 신청 불가
        if (study.getPost().getAuthorId().equals(applicantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인이 개설한 스터디에는 신청할 수 없습니다.");
        }

        CbuMember applicant = cbuMemberRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        StudyApply apply = StudyApply.create(study, applicant);
        studyApplyRepository.save(apply);

        return toStudyApplyInfoDTO(apply);
    }

    /**
     * 스터디 신청 목록 조회
     */
    @Transactional
    public List<StudyApplyDTO.StudyApplyInfoDTO> getApplicants(Long postId) {
        Study study = studyRepository.findByPostId(postId);
        if (study == null) {
            throw new EntityNotFoundException("Study Not Found");
        }

        List<StudyApply> applies = studyApplyRepository.findByStudyId(study.getId());
        return applies.stream()
                .map(this::toStudyApplyInfoDTO)
                .toList();
    }

    /**
     * 스터디 신청 수락/거절
     */
    @Transactional
    public StudyApplyDTO.StudyApplyInfoDTO updateApplyStatus(Long postId, Long applyId, 
                                                              StudyApplyStatus status, Long userId) {
        Study study = studyRepository.findByPostId(postId);
        if (study == null) {
            throw new EntityNotFoundException("Study Not Found");
        }

        // 권한 확인 (스터디 개설자=팀장만 가능)
        validateStudyOwner(study.getPost(), userId);

        StudyApply apply = studyApplyRepository.findByIdAndStudyId(applyId, study.getId())
                .orElseThrow(() -> new EntityNotFoundException("Apply Not Found"));

        apply.changeStatus(status);

        return toStudyApplyInfoDTO(apply);
    }

    /**
     * 스터디 모집 마감 + Group 생성
     */
    @Transactional
    public GroupDTO.GroupCreateResponseDTO closeStudyRecruitment(Long postId, String studyName, Long userId) {
        Study study = studyRepository.findByPostId(postId);
        if (study == null) {
            throw new EntityNotFoundException("Study Not Found");
        }

        Post post = study.getPost();

        // 권한 확인 (스터디 개설자=팀장만 가능)
        validateStudyOwner(post, userId);

        // 이미 마감된 스터디인지 확인
        if (!study.isRecruiting()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 모집이 마감된 스터디입니다.");
        }

        // 모집 상태 변경
        study.updateRecruiting(false);

        // Group 생성 (개설자가 Leader)
        GroupDTO.GroupCreateRequestDTO groupReq = new GroupDTO.GroupCreateRequestDTO();
        // Reflection으로 필드 설정 (DTO에 setter가 없으므로)
        setGroupCreateRequestDTO(groupReq, studyName, 1, 10);

        GroupDTO.GroupCreateResponseDTO groupResponse = groupService.createGroup(groupReq, userId);

        // 수락된 신청자들을 GroupMember로 등록
        List<StudyApply> acceptedApplies = studyApplyRepository.findByStudyIdAndStatus(
                study.getId(), StudyApplyStatus.ACCEPTED);

        for (StudyApply apply : acceptedApplies) {
            // 그룹에 멤버 추가 + 활성화
            GroupDTO.GroupMemberInfoDTO memberInfo = groupService.addGroupMember(
                    groupResponse.getGroupId(), 
                    apply.getApplicant().getCbuMemberId());
            groupService.activateGroupMember(memberInfo.getGroupMemberId(), userId);
        }

        // Study와 Group 연결
        Group group = new Group();
        // GroupRepository를 직접 호출하지 않고 linkGroup에서 처리
        // groupId로 조회하여 연결 필요 - 여기서는 간단히 처리
        study.linkGroup(null); // 추후 개선 필요

        return groupResponse;
    }

    /**
     * StudyApply -> DTO 변환
     */
    private StudyApplyDTO.StudyApplyInfoDTO toStudyApplyInfoDTO(StudyApply apply) {
        return StudyApplyDTO.StudyApplyInfoDTO.builder()
                .applyId(apply.getId())
                .studyId(apply.getStudy().getId())
                .applicantId(apply.getApplicant().getCbuMemberId())
                .applicantName(apply.getApplicant().getName())
                .major(apply.getApplicant().getMajor())
                .grade(apply.getApplicant().getGrade())
                .status(apply.getStatus())
                .createdAt(apply.getCreatedAt())
                .build();
    }

    /**
     * GroupCreateRequestDTO 필드 설정 헬퍼
     */
    private void setGroupCreateRequestDTO(GroupDTO.GroupCreateRequestDTO dto, 
                                           String groupName, int min, int max) {
        try {
            var nameField = dto.getClass().getDeclaredField("groupName");
            nameField.setAccessible(true);
            nameField.set(dto, groupName);

            var minField = dto.getClass().getDeclaredField("minActiveMembers");
            minField.setAccessible(true);
            minField.set(dto, min);

            var maxField = dto.getClass().getDeclaredField("maxActiveMembers");
            maxField.setAccessible(true);
            maxField.set(dto, max);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set GroupCreateRequestDTO fields", e);
        }
    }
}
