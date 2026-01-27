package com.example.cbumanage.service;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.Project;
import com.example.cbumanage.model.enums.ProjectFieldType;
import com.example.cbumanage.repository.ProjectRepository;
import com.example.cbumanage.repository.PostRepository;
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
public class ProjectService {

    private ProjectRepository projectRepository;
    private PostRepository postRepository;
    private PostMapper postMapper;
    private PostService postService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          PostRepository postRepository,
                          PostMapper postMapper,
                          PostService postService) {
        this.projectRepository = projectRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.postService = postService;
    }

    //프로젝트 게시글 생성 메서드
    public Project createProject(PostDTO.ProjectCreateDTO req) {
        Post post = postRepository.findById(req.getPostId()).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        List<String> fields = (req.getRecruitmentFields() != null)
                ? req.getRecruitmentFields()
                : new ArrayList<>();
        Project project = Project.create(post, fields, req.isRecruiting());
        return projectRepository.save(project);
    }

    //프로젝트 상세 조회 메서드
    public PostDTO.ProjectInfoDetailDTO getProjectByPostId(Long postId) {
        Project project = projectRepository.findByPostId(postId);
        return postMapper.toProjectInfoDetailDTO(project);
    }

    //프로젝트 게시글 전체 조회 메서드
    public Page<PostDTO.ProjectListDTO> getPostsByCategory(Pageable pageable,int category){
        Page<Project> projects= projectRepository.findByPostCategoryAndPostIsDeletedFalse(category,pageable);
        return projects.map(project->postMapper.toProjectListDTO(project));
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
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        //권한 확인
        validateProjectOwner(post, userId);
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        postService.updatePost(postUpdateDTO, post);
        Project project = projectRepository.findByPostId(postId);
        PostDTO.ProjectUpdateDTO projectUpdateDTO = postMapper.toPostProjectUpdateDTO(req);
        updateProject(projectUpdateDTO, project);
    }

    //프로젝트 게시글 생성 트랜잭션
    @Transactional
    public PostDTO.PostProjectCreateResponseDTO createPostProject(PostDTO.PostProjectCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req, userId);
        Post post = postService.createPost(postCreateDTO);
        PostDTO.ProjectCreateDTO projectCreateDTO = postMapper.toProjectCreateDTO(req, post.getId());
        Project project = createProject(projectCreateDTO);
        return postMapper.toPostProjectCreateResponseDTO(post, project);
    }

    //프로젝트 게시글 삭제 트랜잭션
    @Transactional
    public void softDeletePost(Long postId,Long userId) {
        Post post=postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Project Not Found"));
        //권한 확인
        validateProjectOwner(post, userId);
        post.delete();
    }

    //프로젝트 모집분야로 조회 트랜잭션
    @Transactional
    public Page<PostDTO.ProjectListDTO> searchByField(ProjectFieldType fields, Pageable pageable) {
        Page<Project> projects = projectRepository.findByRecruitmentFieldsAndPostIsDeletedFalse(fields, pageable);
        return projects.map(project -> postMapper.toProjectListDTO(project));
    }

    //유효 권한 확인 메서드
    private void validateProjectOwner(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "게시글에 대한 권한이 없습니다."
            );
        }
    }
}
