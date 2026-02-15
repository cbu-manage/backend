package com.example.cbumanage.service;

import com.example.cbumanage.dto.*;
import com.example.cbumanage.model.*;
import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.PostReportGroupType;
import com.example.cbumanage.model.enums.Role;
import com.example.cbumanage.repository.*;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.PostReport;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.PostReportRepository;
import com.example.cbumanage.repository.PostRepository;
import com.example.cbumanage.utils.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    private PostRepository postRepository;
    private PostReportRepository postReportRepository;
    private PostMapper postMapper;
    private CbuMemberRepository cbuMemberRepository;
    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;


    @Autowired
    public PostService(PostRepository postRepository, PostReportRepository postReportRepository, PostMapper postMapper, CbuMemberRepository cbuMemberRepository,GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.postRepository = postRepository;
        this.postReportRepository = postReportRepository;
        this.postMapper = postMapper;
        this.cbuMemberRepository = cbuMemberRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
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
        Group group = groupRepository.findById(req.getGroupId());
        PostReport report = PostReport.create(post, req.getGroupId(), req.getType(),req.getDate(),req.getLocation(),req.getReportImage());
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
        Page<Post> posts=postRepository.findByCategoryAndIsDeletedFalse(category,pageable);
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
    보고서 게시글 미리보기 리스트 입니다. 테스트를위해 카테고리는 7로 자동주입해서 사용합니다

    fetch join -> 해결
     */
    public Page<PostDTO.PostReportPreviewDTO> getPostReportPreviewDTOList(Pageable pageable){
        Page<PostDTO.PostReportPreviewDTO> reportPreviewDTOS = postReportRepository.findPostReportPreviews(pageable,7);
        return reportPreviewDTOS;
    }

    /*
    보고서 포스트 자세히 보기 메소드입니다. post와 report를 한번에 가져옵니다
     */
    public PostDTO.PostReportViewDTO getPostReportViewDTO(Long postId,Long userId){
        PostReport report = postReportRepository.findByPostId(postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        CbuMember user = cbuMemberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        boolean isAdmin = user.getRole().contains(Role.ADMIN);
        boolean isAuthor = post.getAuthorId().equals(userId);
        boolean isActiveMember =
                groupMemberRepository.existsActiveMember(
                        userId,
                        report.getGroupId(),
                        GroupMemberStatus.ACTIVE
                );

        if (!(isAdmin || isAuthor || isActiveMember)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return postMapper.toPostReportViewDTO(post, report);
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
        postReport.changeReportImage(postUpdateDTO.getReportImage());
        postReport.changeType(postUpdateDTO.getType());
    }

    /*
    컨트롤러에서 요청을 받아 각 DTO 로 나누고 알맞는 메소드를 호출합니다
    Create 와  마찬가지로 컨트롤러에서 부르는 메소드는 이 메소드이기에, 해당 메소드에 Transactional 를 추가했습니다
     */
    @Transactional
    public void updatePostReport(PostDTO.PostReportUpdateRequestDTO req,Long postId,Long userId) {
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        if(!post.getAuthorId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"NOT POST OWNER");
        }
        PostDTO.PostUpdateDTO postUpdateDTO = postMapper.toPostUpdateDTO(req);
        updatePost(postUpdateDTO,post);
        PostReport report =postReportRepository.findByPostId(postId);
        PostDTO.ReportUpdateDTO reportUpdateDTO=postMapper.topostReportUpdateDTO(req);
        updateReport(reportUpdateDTO,report);
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

    @Transactional
    public void acceptReport(Long postId,Long userId) {
        CbuMember cbuMember = cbuMemberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        if (!cbuMember.getRole().contains(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        PostReport report = postReportRepository.findByPostId(postId);
        report.Accept();
    }



}
