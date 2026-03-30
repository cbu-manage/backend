package com.example.cbumanage.utils;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.model.*;
import com.example.cbumanage.model.enums.PostCategory;
import com.example.cbumanage.model.enums.ProjectFieldType;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.CommentRepository;
import com.example.cbumanage.repository.GroupRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostMapper {

    private final GroupUtil groupUtil;
    private final GroupRepository groupRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final CommentRepository commentRepository;
    @Autowired
    public PostMapper(GroupUtil groupUtil, GroupRepository groupRepository, CbuMemberRepository cbuMemberRepository, CommentRepository commentRepository) {
        this.groupUtil = groupUtil;
        this.groupRepository = groupRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.commentRepository = commentRepository;
    }


    public PostDTO.PostInfoDTO toPostInfoDTO(Post post) {
        CbuMember author = cbuMemberRepository.findById(post.getAuthorId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        return  PostDTO.PostInfoDTO.builder().postId(post.getId())
                .authorId(post.getAuthorId())
                .authorName(author.getName())
                .generation(author.getGeneration())
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
                .category(PostCategory.REPORT.getValue())
                .build();
    }

    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostProjectCreateRequestDTO req, Long userId) {
        return PostDTO.PostCreateDTO.builder()
                .authorId(userId)
                .title(req.title())
                .content(req.content())
                .category(PostCategory.PROJECT.getValue())
                .build();
    }

    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostStudyCreateRequestDTO req, Long userId) {
        return PostDTO.PostCreateDTO.builder()
                .authorId(userId)
                .title(req.getTitle())
                .content(req.getContent())
                .category(PostCategory.STUDY.getValue())
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
                .recruitmentFields(req.recruitmentFields())
                .recruiting(req.recruiting())
                .deadline(req.deadline())
                .build();
    }

    // 프로젝트 생성 응답 DTO 변환
    public PostDTO.PostProjectCreateResponseDTO toPostProjectCreateResponseDTO(Post post, Project project, Group group, CbuMember author) {
        return PostDTO.PostProjectCreateResponseDTO.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .groupId(group.getId())
                .authorGeneration(author!=null?author.getGeneration():null)
                .authorName(author!=null?author.getName():null)
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .recruitmentFields(project.getRecruitmentFields().stream()
                        .map(ProjectFieldType::getDescription) // Enum의 이름(BACKEND 등)을 String으로 변환
                        .collect(Collectors.toList()))
                .recruiting(project.getRecruiting())
                .deadline(project.getDeadline())
                .maxMembers(group.getMaxActiveMembers())
                .category(post.getCategory())
                .build();
    }

    public PostDTO.StudyCreateDTO toStudyCreateDTO(PostDTO.PostStudyCreateRequestDTO req, Long postId) {
        return PostDTO.StudyCreateDTO.builder()
                .postId(postId)
                .studyTags(req.getStudyTags())
                .studyName(req.getStudyName())
                .recruiting(req.getRecruiting())
                .maxMembers(req.getMaxMembers())
                .build();
    }

    public PostDTO.PostStudyCreateResponseDTO toPostStudyCreateResponseDTO(Post post, Study study, Group group, CbuMember author) {
        return PostDTO.PostStudyCreateResponseDTO.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .groupId(group.getId())
                .authorGeneration(author != null ? author.getGeneration() : null)
                .authorName(author != null ? author.getName() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .studyTags(study.getStudyTags())
                .studyName(study.getStudyName())
                .recruiting(study.isRecruiting())
                .maxMembers(study.getMaxMembers())
                .createdAt(post.getCreatedAt())
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
                .title(req.title())
                .content(req.content()).build();
    }

    public PostDTO.PostUpdateDTO toPostUpdateDTO(PostDTO.PostStudyUpdateRequestDTO req) {
        return PostDTO.PostUpdateDTO.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .build();
    }

    public PostDTO.StudyUpdateDTO toStudyUpdateDTO(PostDTO.PostStudyUpdateRequestDTO req) {
        return PostDTO.StudyUpdateDTO.builder()
                .studyTags(req.getStudyTags())
                .studyName(req.getStudyName())
                .maxMembers(req.getMaxMembers())
                .build();
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
                .recruitmentFields(req.recruitmentFields())
                .recruiting(req.recruiting())
                .deadline(req.deadline())
                .build();
    }

    // 프로젝트 게시글 상세 조회 DTO 변환 (로그인 사용자 기준 isLeader, hasApplied 포함, 활동인원/최대인원 표시용 카운트 포함)
    public PostDTO.ProjectInfoDetailDTO toProjectInfoDetailDTO(Project project,
                                                               Long userId,
                                                               boolean isLeader,
                                                               Boolean hasApplied,
                                                               CbuMember author,
                                                               int activeMemberCount,
                                                               int maxMember) {
        Long groupId = project.getGroup() != null ? project.getGroup().getId() : null;
        return PostDTO.ProjectInfoDetailDTO.builder()
                .postId(project.getPost().getId())
                .title(project.getPost().getTitle())
                .content(project.getPost().getContent())
                .recruitmentFields(project.getRecruitmentFields().stream()
                        .map(ProjectFieldType::getDescription)
                        .collect(Collectors.toList()))
                .authorId(project.getPost().getAuthorId())
                .authorGeneration(author!=null?author.getGeneration():null)
                .authorName(author!=null?author.getName():null)
                .groupId(groupId)
                .isLeader(isLeader)
                .hasApplied(hasApplied)
                .createdAt(project.getPost().getCreatedAt())
                .recruiting(project.getRecruiting())
                .deadline(project.getDeadline())
                .viewCount(project.getPost().getViewCount())
                .activeMemberCount(activeMemberCount)
                .maxMembers(maxMember)
                .groupRecruitmentStatus(project.getGroup().getRecruitmentStatus())
                .build();
    }

    // 프로젝트 게시글 목록 조회 DTO 변환 
    public PostDTO.ProjectListDTO toProjectListDTO(Project project, CbuMember author, int activeMemberCount, int maxMembers) {
        return PostDTO.ProjectListDTO.builder()
                .postId(project.getPost().getId())
                .title(project.getPost().getTitle())
                .content(project.getPost().getContent())
                .recruitmentFields(project.getRecruitmentFields().stream()
                        .map(ProjectFieldType::getDescription)
                        .collect(Collectors.toList()))
                .authorId(project.getPost().getAuthorId())
                .authorGeneration(author!=null?author.getGeneration():null)
                .authorName(author!=null?author.getName():null)
                .createdAt(project.getPost().getCreatedAt())
                .recruiting(project.getRecruiting())
                .deadline(project.getDeadline())
                .viewCount(project.getPost().getViewCount())
                .activeMemberCount(activeMemberCount)
                .maxMembers(maxMembers)
                .build();
    }

    // 스터디 게시글 상세 조회 DTO 변환 (활동인원/최대인원 포함)
    public PostDTO.StudyInfoDetailDTO toStudyInfoDetailDTO(Study study,
                                                           boolean isLeader,
                                                           Boolean hasApplied,
                                                           CbuMember author,
                                                           int activeMemberCount,
                                                           int maxMembers) {
        return PostDTO.StudyInfoDetailDTO.builder()
                .postId(study.getPost().getId())
                .title(study.getPost().getTitle())
                .content(study.getPost().getContent())
                .studyTags(study.getStudyTags())
                .studyName(study.getStudyName())
                .authorId(study.getPost().getAuthorId())
                .authorGeneration(author != null ? author.getGeneration() : null)
                .authorName(author != null ? author.getName() : null)
                .createdAt(study.getPost().getCreatedAt())
                .recruiting(study.isRecruiting())
                .activeMemberCount(activeMemberCount)
                .maxMembers(maxMembers)
                .groupId(study.getGroup() != null ? study.getGroup().getId() : null)
                .isLeader(isLeader)
                .hasApplied(hasApplied)
                .viewCount(study.getPost().getViewCount())
                .build();
    }

    // 스터디 게시글 목록 조회 DTO 변환 (activeMemberCount,maxMembes 추가)
    public PostDTO.StudyListDTO toStudyListDTO(Study study, CbuMember author, int activeMemberCount, int maxMembers) {
        return PostDTO.StudyListDTO.builder()
                .postId(study.getPost().getId())
                .title(study.getPost().getTitle())
                .studyTags(study.getStudyTags())
                .studyName(study.getStudyName())
                .authorId(study.getPost().getAuthorId())
                .authorGeneration(author != null ? author.getGeneration() : null)
                .authorName(author != null ? author.getName() : null)
                .createdAt(study.getPost().getCreatedAt())
                .recruiting(study.isRecruiting())
                .activeMemberCount(activeMemberCount)
                .maxMembers(maxMembers)
                .build();
    }

    public PostDTO.PostReportViewDTO toPostReportViewDTO(Post post, PostReport report) {
        return PostDTO.PostReportViewDTO
                .builder()
                .postInfoDTO(toPostInfoDTO(post))
                .reportInfoDTO(toReportInfoDTO(report))
                .build();
    }

    public PostDTO.PostMyPageViewDTO  toPostMyPageViewDTO(Post post,CbuMember author) {
        return PostDTO.PostMyPageViewDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .createdAt(post.getCreatedAt())
                .authorId(author.getCbuMemberId())
                .authorName(author.getName())
                .authorGeneration(author.getGeneration())
                .viewCount(post.getViewCount())
                .commentCount(commentRepository.countByPostId(post.getId()))
                .build();

    }




}
