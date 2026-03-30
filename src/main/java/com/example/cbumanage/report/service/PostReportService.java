package com.example.cbumanage.report.service;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.report.entity.PostReport;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.member.entity.enums.Role;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.report.repository.PostReportRepository;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.group.repository.GroupRepository;
import com.example.cbumanage.group.repository.GroupMemberRepository;
import com.example.cbumanage.post.util.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostReportService {

    private  PostService postService;
    private  PostRepository postRepository;
    private  PostReportRepository postReportRepository;
    private  PostMapper postMapper;
    private  CbuMemberRepository cbuMemberRepository;
    private  GroupRepository groupRepository;
    private  GroupMemberRepository groupMemberRepository;

    @Autowired
    public PostReportService(PostService postService,
                             PostRepository postRepository,
                             PostReportRepository postReportRepository,
                             PostMapper postMapper,
                             CbuMemberRepository cbuMemberRepository,
                             GroupRepository groupRepository,
                             GroupMemberRepository groupMemberRepository
                             )
    {
        this.postService = postService;
        this.postRepository = postRepository;
        this.postReportRepository = postReportRepository;
        this.postMapper = postMapper;
        this.cbuMemberRepository = cbuMemberRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public PostReport createReport(PostDTO.ReportCreateDTO req) {
        Post post = postRepository.findById(req.getPostId()).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        Group group = groupRepository.findById(req.getGroupId());
        PostReport report = PostReport.create(post, req.getGroupId(), req.getType(),req.getDate(),req.getLocation(),req.getReportImage());
        PostReport saved = postReportRepository.save(report);
        return saved;
    }

    @Transactional
    public PostDTO.PostReportCreateResponseDTO createPostReport(PostDTO.PostReportCreateRequestDTO req,Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req,userId);
        Post post = postService.createPost(postCreateDTO);
        PostDTO.ReportCreateDTO reportCreateDTO = postMapper.toReportCreateDTO(req, post.getId());
        PostReport report = createReport(reportCreateDTO);
        return postMapper.toPostReportCreateResponseDTO(post, report);
    }

    /*
보고서 게시글 미리보기 리스트 입니다. 테스트를위해 카테고리는 7로 자동주입해서 사용합니다

fetch join -> 해결
 */
    public Page<PostDTO.PostReportPreviewDTO> getPostReportPreviewDTOList(Pageable pageable){
        Page<PostDTO.PostReportPreviewDTO> reportPreviewDTOS = postReportRepository.findPostReportPreviews(pageable,7);
        return reportPreviewDTOS;
    }

    public Page<PostDTO.PostReportPreviewDTO> getMyPostReportPreviewDTOList(Pageable pageable,Long userId) {
        Page<PostDTO.PostReportPreviewDTO> myReportPreviewDTOS=postReportRepository.findMyPostReportPreviews(pageable,7,userId);
        return myReportPreviewDTOS;
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
        postService.updatePost(postUpdateDTO,post);
        PostReport report =postReportRepository.findByPostId(postId);
        PostDTO.ReportUpdateDTO reportUpdateDTO=postMapper.topostReportUpdateDTO(req);
        updateReport(reportUpdateDTO,report);
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

    public void updateReport(PostDTO.ReportUpdateDTO postUpdateDTO,PostReport postReport) {
        postReport.changeDate(postUpdateDTO.getDate());
        postReport.changeLocation(postUpdateDTO.getLocation());
        postReport.changeReportImage(postUpdateDTO.getReportImage());
        postReport.changeType(postUpdateDTO.getType());
    }
}
