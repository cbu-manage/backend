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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public Post createPost(PostDTO.PostCreateDTO postCreateDTO) {
        CbuMember author = cbuMemberRepository.findById(postCreateDTO.getAuthorId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Post post = Post.create(author.getCbuMemberId(), postCreateDTO.getTitle(), postCreateDTO.getContent(), postCreateDTO.getCategory());
        Post saved = postRepository.save(post);
        return saved;
    }

    public PostReport createReport(PostDTO.ReportCreateDTO req) {
        Post post = postRepository.findById(req.getPostId()).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        PostReport report = PostReport.create(post.getId(), req.getDate(), req.getLocation(), req.getStartImage(), req.getEndImage());
        PostReport saved = postReportRepository.save(report);
        return saved;
    }

    public PostDTO.PostReportCreateResponseDTO createPostReport(PostDTO.PostReportCreateRequestDTO req) {
        PostDTO.PostCreateDTO postCreateDTO = postMapper.toPostCreateDTO(req);
        Post post = createPost(postCreateDTO);
        PostDTO.ReportCreateDTO reportCreateDTO = postMapper.toReportCreateDTO(req, post.getId());
        PostReport report = createReport(reportCreateDTO);
        return postMapper.toPostReportCreateResponseDTO(post, report);
    }
}
