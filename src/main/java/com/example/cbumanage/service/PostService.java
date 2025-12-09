package com.example.cbumanage.service;

import com.example.cbumanage.dto.*;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.PostReport;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.PostReportRepository;
import com.example.cbumanage.repository.PostRepository;
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
    private PostMapper postMapper;
    private CbuMemberRepository cbuMemberRepository;

    @Autowired
    public PostService(PostRepository postRepository, PostReportRepository postReportRepository, PostMapper postMapper, CbuMemberRepository cbuMemberRepository) {
        this.postRepository = postRepository;
        this.postReportRepository = postReportRepository;
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

    /*
    카테고리를 입력받아 페이지로 받아오고, Mapper 를 통해 PostInfoDTO 로 반환되게 만들었습니다
    게시글 목록에서 사용됩니다
     */
    public Page<PostDTO.PostInfoDTO> getPostsByCategory(Pageable pageable,int category){
        Page<Post> posts=postRepository.findByCategory(category,pageable);
        return posts.map(post->postMapper.toPostInfoDTO(post));
    }


    public PostDTO.PostInfoDTO getPostById(Long postId){
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        return postMapper.toPostInfoDTO(post);
    }

    //PostId로  알맞는 Report 를 불러와 DTO 로 변환시켜 반환하는 메소드 입니다
    public PostDTO.ReportInfoDTO getReportByPostId(Long PostId){
        PostReport report = postReportRepository.findByPostId(PostId);
        return postMapper.toReportInfoDTO(report);
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
}
