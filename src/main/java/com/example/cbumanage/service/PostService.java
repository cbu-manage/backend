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

    public Post createPost(PostDTO.PostCreateDTO postCreateDTO) {
        CbuMember author = cbuMemberRepository.findById(postCreateDTO.getAuthorId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Post post = Post.create(author.getCbuMemberId(), postCreateDTO.getTitle(), postCreateDTO.getContent(), postCreateDTO.getCategory());
        Post saved = postRepository.save(post);
        return saved;
    }


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
    updatePostReport 하나의 메소드에 들어온 req 을 각각의 엔티티에 맞춰
    두개의 DTO 로 분리해 각 엔티티의 update 를 수행합니다
     Setter 를 사용하지 않고 클래스 내부에 변환메소드를 만들어 사용합니다
     */
    public void updatePost(PostDTO.PostUpdateDTO postUpdateDTO,Post post) {
        if (postUpdateDTO.getTitle() != null) {
            post.changeTitle(postUpdateDTO.getTitle());
        }
        if (postUpdateDTO.getContent() != null) {
            post.changeContent(postUpdateDTO.getContent());
        }
    }

    public void updateReport(PostDTO.ReportUpdateDTO postUpdateDTO,PostReport postReport) {
        postReport.changeDate(postUpdateDTO.getDate());
        postReport.changeLocation(postUpdateDTO.getLocation());
        postReport.changeReportImage(postUpdateDTO.getReportImage());
        postReport.changeType(postUpdateDTO.getType());
    }

    @Transactional
    public void softDeletePost(Long postId) {
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        post.delete();
    }




}
