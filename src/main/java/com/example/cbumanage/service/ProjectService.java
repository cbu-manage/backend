package com.example.cbumanage.service;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.exception.CustomException;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.Project;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.model.enums.ProjectFieldType;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.GroupRepository;
import com.example.cbumanage.repository.ProjectRepository;
import com.example.cbumanage.repository.PostRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final GroupRepository groupRepository;
    private final PostMapper postMapper;
    private final PostService postService;
    private final GroupService groupService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          PostRepository postRepository,
                          CbuMemberRepository cbuMemberRepository,
                          GroupRepository groupRepository,
                          PostMapper postMapper,
                          PostService postService,
                          GroupService groupService
    ) {
        this.projectRepository = projectRepository;
        this.postRepository = postRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.groupRepository = groupRepository;
        this.postMapper = postMapper;
        this.postService = postService;
        this.groupService= groupService;
    }

    //프로젝트 게시글 생성 메서드
    public Project createProject(PostDTO.ProjectCreateDTO req, Group group) {
        Post post = postRepository.findById(req.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"게시글이 생성되지 않았습니다."));
        List<String> fields = (req.getRecruitmentFields() != null)
                ? req.getRecruitmentFields()
                : new ArrayList<>();
        Project project = Project.create(post, fields, req.isRecruiting(),req.getDeadline(),group);
        return projectRepository.save(project);
    }

    //프로젝트 상세 조회 메서드 (로그인 시 isLeader·hasApplied 반영)
    @Transactional
    public PostDTO.ProjectInfoDetailDTO getProjectByPostId(Long postId, Long userId) {
        Project project = projectRepository.findByPostId(postId)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"해당 게시글을 찾을 수 없습니다."));
        Long groupId = project.getGroup() != null ? project.getGroup().getId() : null;
        Boolean hasApplied = groupService.hasAppliedToGroup(groupId, userId);
        Post post =project.getPost();
        post.upViewCount();
        postRepository.save(post);
        boolean isLeader = userId != null && userId.equals(project.getPost().getAuthorId());
        CbuMember author = cbuMemberRepository.findById(project.getPost().getAuthorId()).orElse(null);
        return postMapper.toProjectInfoDetailDTO(project, userId, isLeader, hasApplied, author);

    }

    //프로젝트 게시글 전체 조회 메서드
    public Page<PostDTO.ProjectListDTO> getPostsByCategory(Pageable pageable, Boolean recruiting, int category) {
        Page<Project> projects = projectRepository.findByCategory(category, recruiting, pageable);
        return mapProjectsToProjectListDTO(projects);
    }

    //내가 작성한 프로젝트 게시글 전체 조회 메서드
    public Page<PostDTO.ProjectListDTO> getMyProjectsByUserId(Pageable pageable, Long userId, int category) {
        Page<Project> projects = projectRepository.findByUserIdAndCategory(userId,category, pageable);
        return mapProjectsToProjectListDTO(projects);
    }

    // 프로젝트 목록 조회 결과에 작성자 정보를 매핑하여 DTO로 변환
    private Page<PostDTO.ProjectListDTO> mapProjectsToProjectListDTO(Page<Project> projects) {
        if (projects.getContent().isEmpty()) {
            return projects.map(p -> postMapper.toProjectListDTO(p, null));
        }
        java.util.Set<Long> authorIds = projects.getContent().stream()
                .map(p -> p.getPost().getAuthorId())
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, CbuMember> authorMap = cbuMemberRepository.findAllById(authorIds).stream()
                .collect(java.util.stream.Collectors.toMap(CbuMember::getCbuMemberId, m -> m));
        return projects.map(p -> postMapper.toProjectListDTO(p, authorMap.get(p.getPost().getAuthorId())));
    }

    //프로젝트 게시글 수정 메서드
    public void updateProject(PostDTO.ProjectUpdateDTO dto, Project project) {
        project.updateRecruitmentFields(dto.getRecruitmentFields());
        project.updateRecruiting(dto.isRecruiting());
    }

    //프로젝트 게시글 수정 트랜잭션
    @Transactional
    public void updatePostProject(PostDTO.PostProjectUpdateRequestDTO req, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 게시글을 찾을 수 없습니다."));
        //권한 확인
        validateProjectOwner(post, userId);
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        postService.updatePost(postUpdateDTO, post);
        Project project = projectRepository.findByPostId(postId).
                orElseThrow(()->new CustomException(ErrorCode.NOT_FOUND,"해당 게시글을 찾을 수 없습니다."));
        PostDTO.ProjectUpdateDTO projectUpdateDTO = postMapper.toPostProjectUpdateDTO(req);
        updateProject(projectUpdateDTO, project);
        groupService.updateGroupMaxMember(project.getGroup().getId(), req.getMaxMember());
        if (project.getGroup() != null) {
            GroupRecruitmentStatus status = req.isRecruiting() ? GroupRecruitmentStatus.OPEN : GroupRecruitmentStatus.CLOSED;
            groupService.updateGroupRecruitment(project.getGroup().getId(), userId, status);
        }
    }

    //프로젝트 게시글 생성 트랜잭션: Post → Project → Group 자동 생성(그룹명=게시글 제목, 최소1명 최대(사용자 설정), 작성자=리더)
    @Transactional
    public PostDTO.PostProjectCreateResponseDTO createPostProject(PostDTO.PostProjectCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req, userId);
        Post post = postService.createPost(postCreateDTO);
        String groupName = post.getTitle() + " #" + post.getId();
        Group group = groupService.createGroup(groupName, post.getAuthorId(),req.getMaxMember());
        PostDTO.ProjectCreateDTO projectCreateDTO = postMapper.toProjectCreateDTO(req, post.getId());
        Project project = createProject(projectCreateDTO,group);
        projectRepository.save(project);
        CbuMember author = cbuMemberRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 유저를 찾을 수 없습니다."));
        return postMapper.toPostProjectCreateResponseDTO(post, project, group, author);
    }

    //프로젝트 게시글 삭제 트랜잭션
    @Transactional
    public void softDeletePost(Long postId,Long userId) {
        Post post=postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 게시글을 찾을 수 없습니다."));
        //권한 확인
        validateProjectOwner(post, userId);
        Project project = projectRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"해당 게시글을 찾을 수 없습니다."));
        post.delete();
        //group도 동시에 soft delete 처리
        project.getGroup().delete();
    }

    //프로젝트 모집분야로 조회 트랜잭션
    @Transactional
    public Page<PostDTO.ProjectListDTO> searchByField(ProjectFieldType fields, Boolean recruiting, Pageable pageable) {
        Page<Project> projects = projectRepository.findByFilters(fields, recruiting,pageable);
        return mapProjectsToProjectListDTO(projects);
    }

    //유효 권한 확인 메서드
    private void validateProjectOwner(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}
