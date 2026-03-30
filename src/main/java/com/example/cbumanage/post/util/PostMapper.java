package com.example.cbumanage.post.util;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.report.entity.PostReport;
import com.example.cbumanage.project.entity.Project;
import com.example.cbumanage.study.entity.Study;
import com.example.cbumanage.group.util.GroupUtil;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.project.entity.enums.ProjectFieldType;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.group.repository.GroupRepository;
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
        return new PostDTO.PostInfoDTO(
                post.getId(),
                author.getName(),
                author.getGeneration(),
                post.getAuthorId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public PostDTO.ReportInfoDTO toReportInfoDTO(PostReport report) {
        Group group = groupRepository.findById(report.getGroupId()).orElseThrow(EntityNotFoundException::new);
        return new PostDTO.ReportInfoDTO(
                report.getLocation(),
                report.getReportImage(),
                report.getDate(),
                groupUtil.toGroupInfoDTO(group),
                report.getType(),
                report.isAccepted()
        );
    }

    /*
    아래는 각 Post{...}CreateRequestDTO 가 CreatePostDTO 를 만들 수 있게 하는 메소드 입니다
    매개변수의 DTO 를 바꾸면서 오버로딩하시면서 메소드 추가 하시면 됩니다
     */
    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostReportCreateRequestDTO req, Long userId) {
        return new PostDTO.PostCreateDTO(userId, req.title(), req.content(), PostCategory.REPORT.getValue());
    }

    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostProjectCreateRequestDTO req, Long userId) {
        return new PostDTO.PostCreateDTO(userId, req.getTitle(), req.getContent(), PostCategory.PROJECT.getValue());
    }

    public PostDTO.PostCreateDTO toPostCreateDTO(PostDTO.PostStudyCreateRequestDTO req, Long userId) {
        return new PostDTO.PostCreateDTO(userId, req.getTitle(), req.getContent(), PostCategory.STUDY.getValue());
    }

    /*
    아래는 게시물의 맞게 CreateDTO 를 반환해주는 메소드 입니다.Post 생성후에 postId를 매개변숯로 추가합니다
     */
    public PostDTO.ReportCreateDTO toReportCreateDTO(PostDTO.PostReportCreateRequestDTO req, Long postId) {
        return new PostDTO.ReportCreateDTO(
                postId,
                req.location(),
                req.reportImage(),
                req.date(),
                req.groupId(),
                req.type()
        );
    }


    /*
    아래는 각 게시물에 맞는 CreateResponseDTO 를 반환해 주는 메소드 입니다
     */
    public PostDTO.PostReportCreateResponseDTO toPostReportCreateResponseDTO(Post post, PostReport report) {
        Group group = groupRepository.findById(report.getGroupId()).orElseThrow(() -> new EntityNotFoundException("Group not found"));
        return new PostDTO.PostReportCreateResponseDTO(
                post.getId(),
                post.getAuthorId(),
                groupUtil.toGroupInfoDTO(group),
                post.getTitle(),
                post.getContent(),
                report.getLocation(),
                report.getReportImage(),
                report.getDate(),
                post.getCreatedAt(),
                post.getCategory(),
                report.getType()
        );
    }

    public PostDTO.ProjectCreateDTO toProjectCreateDTO(PostDTO.PostProjectCreateRequestDTO req,Long postId) {
        return PostDTO.ProjectCreateDTO.builder()
                .postId(postId)
                .recruitmentFields(req.getRecruitmentFields())
                .recruiting(req.getRecruiting())
                .deadline(req.getDeadline())
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
        return new PostDTO.PostUpdateDTO(req.title(), req.content());
    }

    public PostDTO.PostUpdateDTO toPostUpdateDTO(PostDTO.PostProjectUpdateRequestDTO req) {
        return new PostDTO.PostUpdateDTO(req.getTitle(), req.getContent());
    }

    public PostDTO.PostUpdateDTO toPostUpdateDTO(PostDTO.PostStudyUpdateRequestDTO req) {
        return new PostDTO.PostUpdateDTO(req.getTitle(), req.getContent());
    }

    public PostDTO.StudyUpdateDTO toStudyUpdateDTO(PostDTO.PostStudyUpdateRequestDTO req) {
        return PostDTO.StudyUpdateDTO.builder()
                .studyTags(req.getStudyTags())
                .studyName(req.getStudyName())
                .maxMembers(req.getMaxMembers())
                .build();
    }

    public PostDTO.ReportUpdateDTO topostReportUpdateDTO(PostDTO.PostReportUpdateRequestDTO req) {
        return new PostDTO.ReportUpdateDTO(
                req.location(),
                req.reportImage(),
                req.date(),
                req.groupId(),
                req.type()
        );
    }

    public PostDTO.ProjectUpdateDTO toPostProjectUpdateDTO(PostDTO.PostProjectUpdateRequestDTO req) {
        return PostDTO.ProjectUpdateDTO.builder()
                .recruitmentFields(req.getRecruitmentFields())
                .recruiting(req.getRecruiting())
                .deadline(req.getDeadline())
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
        return new PostDTO.PostReportViewDTO(toPostInfoDTO(post), toReportInfoDTO(report));
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
