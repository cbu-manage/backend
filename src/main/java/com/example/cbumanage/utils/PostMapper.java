package com.example.cbumanage.utils;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.PostReport;
import com.example.cbumanage.model.PostProject;
import com.example.cbumanage.repository.PostReportRepository;
import com.example.cbumanage.repository.PostProjectRepository;
import com.example.cbumanage.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {


    public PostDTO.PostInfoDTO toPostInfoDTO(Post post) {
        return  PostDTO.PostInfoDTO.builder().postId(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt()).build();
    }

    public PostDTO.ReportInfoDTO toReportInfoDTO(PostReport report) {
        return PostDTO.ReportInfoDTO.builder()
                .location(report.getLocation())
                .startImage(report.getStartImage())
                .endImage(report.getEndImage())
                .date(report.getDate())
                .build();
    }

    public PostDTO.ProjectInfoDTO toProjectInfoDTO(PostProject project) {
        return PostDTO.ProjectInfoDTO.builder()
                .recruitmentField(project.getRecruitmentField())
                .techStack(project.getTechStack())
                .recruiting(project.isRecruiting())
                .build();
    }

    /*
    아래는 각 Post{...}CreateRequestDTO 가 CreatePostDTO 를 만들 수 있게 하는 메소드 입니다
    매개변수의 DTO 를 바꾸면서 오버로딩하시면서 메소드 추가 하시면 됩니다
     */
    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostReportCreateRequestDTO req, Long userId) {
        return PostDTO.PostCreateDTO.builder()
                .authorId(userId)
                .title(req.getTitle())
                .content(req.getContent()).build();
    }

    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostProjectCreateRequestDTO req, Long userId) {
        return PostDTO.PostCreateDTO.builder()
                .authorId(userId)
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.getCategory())
                .build();
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

    public PostDTO.ProjectCreateDTO toProjectCreateDTO(PostDTO.PostProjectCreateRequestDTO req,Long postId) {
        return PostDTO.ProjectCreateDTO.builder()
                .postId(postId)
                .recruitmentField(req.getRecruitmentField())
                .techStack(req.getTechStack())
                .recruiting(req.isRecruiting())
                .build();
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

    public PostDTO.PostProjectCreateResponseDTO toPostProjectCreateResponseDTO(Post post, PostProject project) {
        return PostDTO.PostProjectCreateResponseDTO.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .recruitmentField(project.getRecruitmentField())
                .techStack(project.getTechStack())
                .recruiting(project.isRecruiting())
                .category(post.getCategory())
                .build();
    }

    /*
    아래는 Post{...}UpdateRequestDTO 를 각 카테고리에 맞게 분리해 주는 Mapper 입니다
     */

    public PostDTO.PostUpdateDTO toPostUpdateDTO(PostDTO.PostReportUpdateRequestDTO req) {
        return PostDTO.PostUpdateDTO.builder()
                .title(req.getTitle())
                .content(req.getContent()).build();
    }

    public PostDTO.PostUpdateDTO toPostUpdateDTO(PostDTO.PostProjectUpdateRequestDTO req) {
        return PostDTO.PostUpdateDTO.builder()
                .title(req.getTitle())
                .content(req.getContent()).build();
    }

    public PostDTO.ReportUpdateDTO topostReportUpdateDTO(PostDTO.PostReportUpdateRequestDTO req) {
        return PostDTO.ReportUpdateDTO.builder()
                .location(req.getLocation())
                .StartImage(req.getStartImage())
                .endImage(req.getEndImage())
                .date(req.getDate())
                .build();
    }

    public PostDTO.ProjectUpdateDTO toPostProjectUpdateDTO(PostDTO.PostProjectUpdateRequestDTO req) {
        return PostDTO.ProjectUpdateDTO.builder()
                .recruitmentField(req.getRecruitmentField())
                .techStack(req.getTechStack())
                .recruiting(req.isRecruiting())
                .build();
    }
}
