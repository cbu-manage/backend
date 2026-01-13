package com.example.cbumanage.service;

import com.example.cbumanage.dto.*;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.PostReport;
import com.example.cbumanage.model.PostStudy;
import com.example.cbumanage.model.PostProject;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.PostReportRepository;
import com.example.cbumanage.repository.PostProjectRepository;
import com.example.cbumanage.repository.PostRepository;
import com.example.cbumanage.repository.PostStudyRepository;
import com.example.cbumanage.utils.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private PostRepository postRepository;
    private PostReportRepository postReportRepository;
    private PostStudyRepository postStudyRepository;
    private PostProjectRepository postProjectRepository;
    private PostMapper postMapper;
    private CbuMemberRepository cbuMemberRepository;


    @Autowired
    public PostService(PostRepository postRepository, PostReportRepository postReportRepository, PostStudyRepository postStudyRepository, PostMapper postMapper, CbuMemberRepository cbuMemberRepository,
                       PostProjectRepository postProjectRepository) {
        this.postRepository = postRepository;
        this.postReportRepository = postReportRepository;
        this.postStudyRepository = postStudyRepository;
        this.postProjectRepository = postProjectRepository;
        this.postMapper = postMapper;
        this.cbuMemberRepository = cbuMemberRepository;
    }

    /*
    CreatePostReport 하나의 메소드에서 createPost,createReport 를 호출하여 나눠서 작성 후
    ResponseDTO 로 반환하도록 만들었습니다
     */
    public Post createPost(PostDTO.PostCreateDTO postCreateDTO) {
        CbuMember author = cbuMemberRepository.findById(postCreateDTO.getAuthorId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Post post = Post.create(author.getCbuMemberId(), postCreateDTO.getTitle(), postCreateDTO.getContent(), postCreateDTO.getCategory());
        Post saved = postRepository.save(post);
        return saved;
    }

    public PostReport createReport(PostDTO.ReportCreateDTO req) {
        Post post = postRepository.findById(req.getPostId()).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostReport report = PostReport.create(post, req.getDate(), req.getLocation(), req.getStartImage(), req.getEndImage());
        PostReport saved = postReportRepository.save(report);
        return saved;
    }

    public PostStudy createStudy(PostDTO.StudyCreateDTO req) {
        Post post = postRepository.findById(req.getPostId()).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostStudy study = PostStudy.create(post, req.isStatus());
        return postStudyRepository.save(study);
    }

    public PostProject createProject(PostDTO.ProjectCreateDTO req){
        Post post = postRepository.findById(req.getPostId()).orElseThrow(() ->new EntityNotFoundException("Post Not Found"));
        PostProject project=PostProject.create(post, req.getRecruitmentField(), req.getTechStack(), req.isRecruiting());
        return postProjectRepository.save(project);
    }

    /*
        컨트롤러로 부터 req 을 받아 각 DTO 로 나눈 후 알맞는 메소드를 불러와 저장시키는 메소드입니다
        실제로 컨트롤러에서 사용되는 메소드는 이 메소드이기에, 여기에 Transactional 를 사용했습니다
         */
    @Transactional
    public PostDTO.PostReportCreateResponseDTO createPostReport(PostDTO.PostReportCreateRequestDTO req,Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req,userId);
        Post post = createPost(postCreateDTO);
        PostDTO.ReportCreateDTO reportCreateDTO = postMapper.toReportCreateDTO(req, post.getId());
        PostReport report = createReport(reportCreateDTO);
        return postMapper.toPostReportCreateResponseDTO(post, report);
    }

    @Transactional
    public PostDTO.PostStudyCreateResponseDTO createPostStudy(PostDTO.PostStudyCreateRequestDTO req,Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req,userId);
        Post post = createPost(postCreateDTO);
        PostDTO.StudyCreateDTO studyCreateDTO = postMapper.toStudyCreateDTO(req, post.getId());
        PostStudy study = createStudy(studyCreateDTO);
        return postMapper.toPostStudyCreateResponseDTO(post, study);
    }

    @Transactional
    public PostDTO.PostProjectCreateResponseDTO createPostProject(PostDTO.PostProjectCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req,userId);
        Post post = createPost(postCreateDTO);
        PostDTO.ProjectCreateDTO projectCreateDTO = postMapper.toProjectCreateDTO(req, post.getId());
        PostProject project = createProject(projectCreateDTO);
        return postMapper.toPostProjectCreateResponseDTO(post, project);
    }

    /*
    카테고리를 입력받아 페이지로 받아오고, Mapper 를 통해 PostInfoDTO 로 반환되게 만들었습니다
    게시글 목록에서 사용됩니다
     */
    public Page<PostDTO.PostInfoDTO> getPostsByCategory(Pageable pageable,int category){
        Page<Post> posts=postRepository.findByCategoryAndIsDeletedFalse(category,pageable);
        return posts.map(post->postMapper.toPostInfoDTO(post));
    }


    public PostDTO.PostInfoDTO getPostById(Long postId){
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        return postMapper.toPostInfoDTO(post);
    }

    public PostDTO.ReportInfoDTO getReportByPostId(Long PostId){
        PostReport report = postReportRepository.findByPostId(PostId);
        return postMapper.toReportInfoDTO(report);
    }

    public PostDTO.StudyInfoDTO getStudyByPostId(Long PostId){
        PostStudy study = postStudyRepository.findByPostId(PostId);
        return postMapper.toStudyInfoDTO(study);
    }

    public PostDTO.ProjectInfoDTO getProjectByPostId(Long PostId){
        PostProject project = postProjectRepository.findByPostId(PostId);
        return postMapper.toProjectInfoDTO(project);
    }

    /*
    updatePostReport 하나의 메소드에 들어온 req 을 각각의 엔티티에 맞춰
    두개의 DTO 로 분리해 각 엔티티의 update 를 수행합니다
     Setter 를 사용하지 않고 클래스 내부에 변환메소드를 만들어 사용합니다
     */
    public void updatePost(PostDTO.PostUpdateDTO postUpdateDTO,Post post) {
        post.changeTitle(postUpdateDTO.getTitle());
        post.changeContent(postUpdateDTO.getContent());
    }

    public void updateReport(PostDTO.ReportUpdateDTO postUpdateDTO,PostReport postReport) {
        postReport.changeDate(postUpdateDTO.getDate());
        postReport.changeLocation(postUpdateDTO.getLocation());
        postReport.changeStartImage(postUpdateDTO.getStartImage());
        postReport.changeEndImage(postUpdateDTO.getEndImage());
    }

    public void updateStudy(PostDTO.StudyUpdateDTO studyUpdateDTO,PostStudy postStudy) {
        postStudy.changeStatus(studyUpdateDTO.isStatus());
    }

    public void updateProject(PostDTO.ProjectUpdateDTO dto, PostProject project) {
        project.changeRecruitmentField(dto.getRecruitmentField());
        project.changeTechStack(dto.getTechStack());
        project.changeRecruiting(dto.isRecruiting());
    }

    /*
    컨트롤러에서 요청을 받아 각 DTO 로 나누고 알맞는 메소드를 호출합니다
    Create 와  마찬가지로 컨트롤러에서 부르는 메소드는 이 메소드이기에, 해당 메소드에 Transactional 를 추가했습니다
     */
    @Transactional
    public void updatePostReport(PostDTO.PostReportUpdateRequestDTO req,Long postId) {
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        updatePost(postUpdateDTO,post);
        PostReport report =postReportRepository.findByPostId(postId);
        PostDTO.ReportUpdateDTO reportUpdateDTO=postMapper.topostReportUpdateDTO(req);
        updateReport(reportUpdateDTO,report);
    }

    @Transactional
    public void updatePostStudy(PostDTO.PostStudyUpdateRequestDTO req,Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        updatePost(postUpdateDTO, post);
        PostStudy study = postStudyRepository.findByPostId(postId);
        PostDTO.StudyUpdateDTO studyUpdateDTO = postMapper.toStudyUpdateDTO(req);
        updateStudy(studyUpdateDTO, study);
    }

    @Transactional
    public void updatePostProject(PostDTO.PostProjectUpdateRequestDTO req, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        updatePost(postUpdateDTO, post);
        PostProject project = postProjectRepository.findByPostId(postId);
        PostDTO.ProjectUpdateDTO projectUpdateDTO = postMapper.toPostProjectUpdateDTO(req);
        updateProject(projectUpdateDTO, project);
    }

    @Transactional
    public void deletePostById(Long postId) {
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        postRepository.delete(post);
    }

    @Transactional
    public void softDeletePost(Long postId) {
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        post.delete();
    }
}
