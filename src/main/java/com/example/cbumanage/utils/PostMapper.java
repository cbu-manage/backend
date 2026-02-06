package com.example.cbumanage.utils;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.PostReport;
import com.example.cbumanage.model.Project;
import com.example.cbumanage.model.enums.ProjectFieldType;
import com.example.cbumanage.repository.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostMapper {

    private final GroupUtil groupUtil;
    private final GroupRepository groupRepository;

    @Autowired
    public PostMapper(GroupUtil groupUtil, GroupRepository groupRepository) {
        this.groupUtil = groupUtil;
        this.groupRepository = groupRepository;
    }


    public PostDTO.PostInfoDTO toPostInfoDTO(Post post) {
        return  PostDTO.PostInfoDTO.builder().postId(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt()).build();
    }

    public PostDTO.ReportInfoDTO toReportInfoDTO(PostReport report) {
        Group group = groupRepository.findById(report.getGroupId()).orElseThrow(EntityNotFoundException::new);

        return PostDTO.ReportInfoDTO.builder()
                .location(report.getLocation())
                .reportImage(report.getReportImage())
                .date(report.getDate())
                .type(report.getType())
                .groupInfoDTO(groupUtil.toGroupInfoDTO(group))
                .isAccepted(report.isAccepted())
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
                .content(req.getContent())
                .category(req.getCategory())
                .build();
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
                reportImage(req.getReportImage()).
                date(req.getDate()).
                groupId(req.getGroupId()).
                type(req.getType()).
                build();
    }


    /*
    아래는 각 게시물에 맞는 CreateResponseDTO 를 반환해 주는 메소드 입니다
     */
    public PostDTO.PostReportCreateResponseDTO toPostReportCreateResponseDTO(Post post, PostReport report) {
        Group group = groupRepository.findById(report.getGroupId()).orElseThrow(() -> new EntityNotFoundException("Group not found"));

        return PostDTO.PostReportCreateResponseDTO.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .location(report.getLocation())
                .reportImage(report.getReportImage())
                .createdAt(post.getCreatedAt())
                .date(report.getDate())
                .category(post.getCategory())
                .groupInfoDTO(groupUtil.toGroupInfoDTO(group))
                .type(report.getType())
                .build();

    }

    public PostDTO.ProjectCreateDTO toProjectCreateDTO(PostDTO.PostProjectCreateRequestDTO req,Long postId) {
        return PostDTO.ProjectCreateDTO.builder()
                .postId(postId)
                .recruitmentFields(req.getRecruitmentFields())
                .recruiting(req.isRecruiting())
                .build();
    }

    // 프로젝트 생성 응답 DTO 변환
    public PostDTO.PostProjectCreateResponseDTO toPostProjectCreateResponseDTO(Post post, Project project) {
        return PostDTO.PostProjectCreateResponseDTO.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .recruitmentFields(project.getRecruitmentFields().stream()
                        .map(ProjectFieldType::getDescription) // Enum의 이름(BACKEND 등)을 String으로 변환
                        .collect(Collectors.toList()))
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
                .reportImage(req.getReportImage())
                .date(req.getDate())
                .groupId(req.getGroupId())
                .type(req.getType())
                .build();
    }

    public PostDTO.ProjectUpdateDTO toPostProjectUpdateDTO(PostDTO.PostProjectUpdateRequestDTO req) {
        return PostDTO.ProjectUpdateDTO.builder()
                .recruitmentFields(req.getRecruitmentFields())
                .recruiting(req.isRecruiting())
                .build();
    }

    // 프로젝트 게시글 상세 조회 DTO 변환
    public PostDTO.ProjectInfoDetailDTO toProjectInfoDetailDTO(Project project) {
        return PostDTO.ProjectInfoDetailDTO.builder()
                .postId(project.getPost().getId())
                .title(project.getPost().getTitle())
                .content(project.getPost().getContent())
                .recruitmentFields(project.getRecruitmentFields().stream()
                        .map(ProjectFieldType::getDescription)
                        .collect(Collectors.toList()))
                .authorId(project.getPost().getAuthorId())
                .createdAt(project.getPost().getCreatedAt())
                .recruiting(project.isRecruiting())
                .build();
    }

    // 프로젝트 게시글 목록 조회 DTO 변환
    public PostDTO.ProjectListDTO toProjectListDTO(Project project) {
        return PostDTO.ProjectListDTO.builder()
                .postId(project.getPost().getId())
                .title(project.getPost().getTitle())
                .recruitmentFields(project.getRecruitmentFields().stream()
                        .map(ProjectFieldType::getDescription)
                        .collect(Collectors.toList()))
                .authorId(project.getPost().getAuthorId())
                .createdAt(project.getPost().getCreatedAt())
                .recruiting(project.isRecruiting())
                .build();
    }


    public PostDTO.PostReportViewDTO toPostReportViewDTO(Post post, PostReport report) {
        return PostDTO.PostReportViewDTO
                .builder()
                .postInfoDTO(toPostInfoDTO(post))
                .reportInfoDTO(toReportInfoDTO(report))
                .build();
    }


}
