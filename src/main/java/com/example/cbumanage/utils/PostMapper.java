package com.example.cbumanage.utils;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.PostReport;
import com.example.cbumanage.repository.PostReportRepository;
import com.example.cbumanage.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private PostRepository postRepository;
    @Autowired
    public PostMapper(PostRepository postRepository, PostReportRepository postReportRepository) {
        this.postRepository = postRepository;
    }

    public PostDTO.PostInfoDTO toPostInfoDTO(Post post) {
        return  PostDTO.PostInfoDTO.builder().postId(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt()).build();
    }

    /*
    아래는 각 Post{...}CreateRequestDTO 가 CreatePostDTO 를 만들 수 있게 하는 메소드 입니다
    매개변수의 DTO 를 바꾸면서 오버로딩하시면서 메소드 추가 하시면 됩니다
     */
    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostReportCreateRequestDTO req) {
        return PostDTO.PostCreateDTO.builder()
                .authorId(req.getAuthorId())
                .title(req.getTitle())
                .content(req.getContent()).build();
    }


    /*
    아래는 게시물의 맞게 CreateDTO 를 반환해주는 메소드 입니다.Post 생성후에 postId를 매개변숯로 추가합니다
     */
    public PostDTO.ReportCreateDTO toReportCreateDTO(PostDTO.PostReportCreateRequestDTO req,Long postId) {
        return PostDTO.ReportCreateDTO.builder().
                postId(postId).
                location(req.getLocation()).
                startImage(req.getStartImage()).
                endImage(req.getEndImage()).
                date(req.getDate()).build();
    }

    /*
    아래는 각 게시물에 맞는 CreateResponseDTO 를 반환해 주는 메소드 입니다
     */
    public PostDTO.PostReportCreateResponseDTO toPostReportCreateResponseDTO(Post post, PostReport report) {
        return PostDTO.PostReportCreateResponseDTO.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .location(report.getLocation())
                .startImage(report.getStartImage())
                .endImage(report.getEndImage())
                .createdAt(post.getCreatedAt())
                .date(report.getDate())
                .category(post.getCategory())
                .build();

    }


}
