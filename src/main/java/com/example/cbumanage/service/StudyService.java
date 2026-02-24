package com.example.cbumanage.service;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.dto.StudyApplyDTO;
import com.example.cbumanage.exception.CustomException;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.Study;
import com.example.cbumanage.model.StudyApply;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.StudyApplyStatus;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.PostRepository;
import com.example.cbumanage.repository.GroupRepository;
import com.example.cbumanage.repository.StudyApplyRepository;
import com.example.cbumanage.repository.StudyRepository;
import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.utils.PostMapper;
import com.example.cbumanage.utils.StudyApplyMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostService postService;
    private final StudyApplyRepository studyApplyRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final StudyApplyMapper studyApplyMapper;

    @Autowired
    public StudyService(StudyRepository studyRepository,
                        PostRepository postRepository,
                        PostMapper postMapper,
                        PostService postService,
                        StudyApplyRepository studyApplyRepository,
                        CbuMemberRepository cbuMemberRepository,
                        GroupService groupService,
                        GroupRepository groupRepository,
                        StudyApplyMapper studyApplyMapper) {
        this.studyRepository = studyRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.postService = postService;
        this.studyApplyRepository = studyApplyRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.groupService = groupService;
        this.groupRepository = groupRepository;
        this.studyApplyMapper = studyApplyMapper;
    }

    @Transactional
    public Study createStudy(PostDTO.StudyCreateDTO req) {
        Post post = postRepository.findById(req.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        List<String> tags = (req.getStudyTags() != null) ? req.getStudyTags() : new ArrayList<>();
        Study study = Study.create(post, tags, req.getStudyName(), req.getMaxMembers(), req.isRecruiting());
        return studyRepository.save(study);
    }

    // 스터디 상세 조회
    @Transactional
    public PostDTO.StudyInfoDetailDTO getStudyByPostId(Long postId) {
        Study study = getActiveStudy(postId);
        return postMapper.toStudyInfoDetailDTO(study);
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
            long acceptedCount = studyApplyRepository.countByStudyIdAndStatus(
                    study.getId(), StudyApplyStatus.ACCEPTED);

            if (dto.getMaxMembers() <= acceptedCount) {
                throw new CustomException(ErrorCode.INVALID_REQUEST,
                        "이미 수락된 인원(" + acceptedCount + "명)보다 작거나 같은 값으로 변경할 수 없습니다.");
            }
            study.changeMaxMembers(dto.getMaxMembers());
        }
    }

    // Post + Study 한번에 생성
    @Transactional
    public PostDTO.PostStudyCreateResponseDTO createPostStudy(PostDTO.PostStudyCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req, userId);
        Post post = postService.createPost(postCreateDTO);
        PostDTO.StudyCreateDTO studyCreateDTO = postMapper.toStudyCreateDTO(req, post.getId());
        Study study = createStudy(studyCreateDTO);
        return postMapper.toPostStudyCreateResponseDTO(post, study);
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

    // 소프트 삭제
    @Transactional
    public void softDeletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."));
        if (post.isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "이미 삭제된 게시글입니다.");
        }
        validateStudyOwner(post, userId);
        post.delete();
    }

    // 태그 정확히 일치 검색
    @Transactional
    public Page<PostDTO.StudyListDTO> searchByTag(String tag, Pageable pageable) {
        Page<Study> studies = studyRepository.findByExactTagAndPostIsDeletedFalse(tag, pageable);
        return studies.map(study -> postMapper.toStudyListDTO(study));
    }

    /**
     * 스터디 참가 신청.
     * study_apply 테이블에 (study_id, applicant_id) UniqueConstraint가 걸려있어서
     * 취소/거절된 기존 레코드가 있으면 새로 INSERT 하지 않고 reapply()로 PENDING 복구한다.
     */
    @Transactional
    public StudyApplyDTO.StudyApplyInfoDTO applyStudy(Long postId, Long applicantId) {
        Study study = getActiveStudy(postId);

        if (!study.isRecruiting()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "모집이 종료된 스터디입니다.");
        }

        if (study.getPost().getAuthorId().equals(applicantId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "본인이 개설한 스터디에는 신청할 수 없습니다.");
        }

        CbuMember applicant = cbuMemberRepository.findById(applicantId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

        Optional<StudyApply> existingApply = studyApplyRepository.findByStudyIdAndApplicantCbuMemberId(
                study.getId(), applicantId);

        StudyApply apply;
        if (existingApply.isPresent()) {
            apply = existingApply.get();
            if (apply.getStatus() == StudyApplyStatus.PENDING || apply.getStatus() == StudyApplyStatus.ACCEPTED) {
                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 신청한 스터디입니다.");
            }
            // 취소/거절 상태 -> 재신청
            apply.reapply();
        } else {
            apply = StudyApply.create(study, applicant);
            studyApplyRepository.save(apply);
        }

        return studyApplyMapper.toStudyApplyInfoDTO(apply);
    }

    // 신청 취소 (본인만, PENDING만)
    @Transactional
    public void cancelApply(Long postId, Long userId) {
        Study study = getActiveStudy(postId);

        StudyApply apply = studyApplyRepository.findByStudyIdAndApplicantCbuMemberId(study.getId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신청 내역을 찾을 수 없습니다."));

        if (apply.getStatus() != StudyApplyStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "대기 중인 신청만 취소할 수 있습니다.");
        }

        apply.cancel();
    }

    // 신청자 목록 조회 (팀장만). 취소된 건 빼고 보여줌.
    @Transactional
    public List<StudyApplyDTO.StudyApplyInfoDTO> getApplicants(Long postId, Long userId) {
        Study study = getActiveStudy(postId);
        validateStudyOwner(study.getPost(), userId);

        List<StudyApply> applies = studyApplyRepository.findByStudyIdAndStatusNot(study.getId(), StudyApplyStatus.CANCELLED);
        return applies.stream()
                .map(studyApplyMapper::toStudyApplyInfoDTO)
                .toList();
    }

    /**
     * 신청 수락/거절 (팀장만).
     * acceptedCount + 1 >= maxMembers 이면 수락 거부.
     */
    @Transactional
    public StudyApplyDTO.StudyApplyInfoDTO updateApplyStatus(Long postId, Long applyId,
                                                             StudyApplyStatus status, Long userId) {
        Study study = studyRepository.findByPostIdForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));
        if (study.getPost().isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 게시글입니다.");
        }
        validateStudyOwner(study.getPost(), userId);

        if (status != StudyApplyStatus.ACCEPTED && status != StudyApplyStatus.REJECTED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "ACCEPTED 또는 REJECTED만 허용됩니다.");
        }

        if (!study.isRecruiting()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "모집이 마감된 스터디입니다.");
        }

        if (status == StudyApplyStatus.ACCEPTED) {
            long acceptedCount = studyApplyRepository.countByStudyIdAndStatus(
                    study.getId(), StudyApplyStatus.ACCEPTED);
            if (acceptedCount + 1 >= study.getMaxMembers()) {
                throw new CustomException(ErrorCode.INVALID_REQUEST,
                        "모집 인원이 가득 찼습니다. (최대 " + study.getMaxMembers() + "명, 팀장 포함)");
            }
        }

        StudyApply apply = studyApplyRepository.findByIdAndStudyId(applyId, study.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신청 정보를 찾을 수 없습니다."));

        if (apply.getStatus() != StudyApplyStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "대기 중인 신청만 수락/거절할 수 있습니다.");
        }

        apply.changeStatus(status);

        return studyApplyMapper.toStudyApplyInfoDTO(apply);
    }

    /**
     * 모집 마감하면서 Group까지 자동으로 만들어주는 메서드.
     * 마감 → PENDING 일괄 거절 → Group 생성 → 수락자 멤버 등록 순서로 진행.
     */
    @Transactional
    public GroupDTO.GroupCreateResponseDTO closeStudyRecruitment(Long postId, Long userId) {
        Study study = studyRepository.findByPostIdForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스터디를 찾을 수 없습니다."));

        if (study.getPost().isDeleted()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 게시글입니다.");
        }

        validateStudyOwner(study.getPost(), userId);

        if (!study.isRecruiting()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 모집이 마감된 스터디입니다.");
        }

        study.updateRecruiting(false);

        // 아직 대기 중인 신청은 일괄 거절
        List<StudyApply> pendingApplies = studyApplyRepository.findByStudyIdAndStatus(
                study.getId(), StudyApplyStatus.PENDING);
        for (StudyApply pending : pendingApplies) {
            pending.changeStatus(StudyApplyStatus.REJECTED);
        }

        List<StudyApply> acceptedApplies = studyApplyRepository.findByStudyIdAndStatus(
                study.getId(), StudyApplyStatus.ACCEPTED);

        if (acceptedApplies.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "수락된 신청자가 없습니다. 최소 1명 이상 수락해야 합니다.");
        }

        GroupDTO.GroupCreateResponseDTO groupResponse =
                groupService.createGroupInternal(study.getStudyName(), 1, study.getMaxMembers(), userId);

        // TODO: 현재 그룹에 멤버를 추가할 때 대기 상태로 추가 -> 활동 상태로 변경 2단계로 처리가 되는 문제
        //  GroupService.addGroupMember()가 프로젝트 모집과 공유되어 있어 항상 대기상태로만 생성 됨 (프로젝트 모집 방식이 스터디 모집이랑 달라서..)
        for (StudyApply apply : acceptedApplies) {
            GroupDTO.GroupMemberInfoDTO memberInfo = groupService.addGroupMember(
                    groupResponse.getGroupId(),
                    apply.getApplicant().getCbuMemberId());
            groupService.updateStatusGroupMember(memberInfo.getGroupMemberId(), userId, GroupMemberStatus.ACTIVE);
        }

        Group group = groupRepository.findById(groupResponse.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "생성된 그룹을 찾을 수 없습니다."));
        study.linkGroup(group);

        return groupResponse;
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
