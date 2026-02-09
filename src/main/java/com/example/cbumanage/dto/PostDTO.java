package com.example.cbumanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.example.cbumanage.model.enums.PostReportGroupType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/*
Post 에 관한 DTO 들은 전부 여기서 관리하고자 합니다
 */
public class PostDTO {


    /*
    Post 의 핵심 내용을 가지고 있는 DTO 입니다.
    게시글 목록, 게시글 상세 불러오기 등을 할때  해당 DTO 를 불러옵니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostInfoDTO{
        private Long postId;

        private Long authorId;

        private String title;

        private String content;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        @Builder
        public PostInfoDTO(
                Long postId,
                Long authorId,
                String title,
                String content,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        )
        {
            this.postId = postId;
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

    }

    /*
    Post-Report 의 핵심 내용을 가지고 있는 DTO 입니다.
    보고서 게시글 상세보기를 할때 해당 DTO 가 PostInfoDTO 와 함께 불러와 집니다
     */
    @Getter
    @NoArgsConstructor
    public static class ReportInfoDTO{

        private String location;

        private String reportImage;

        private LocalDateTime date;

        //그룹의 정보를 담고 있습니다
        private GroupDTO.GroupInfoDTO groupInfoDTO;

        private PostReportGroupType type;

        private boolean isAccepted;

        @Builder
        public ReportInfoDTO(String location,
                             String reportImage,
                             LocalDateTime date,
                             GroupDTO.GroupInfoDTO groupInfoDTO,
                             PostReportGroupType type,
                             boolean isAccepted) {
            this.location = location;
            this.reportImage = reportImage;
            this.date = date;
            this.groupInfoDTO = groupInfoDTO;
            this.type = type;
            this.isAccepted = isAccepted;

        }
    }



    /*
    보고서 게시물을 만들때, 유저가 보내는 데이터를 담는 DTO 입니다.
    해당 DTO 를 Controller 에서 받아와, Service 에서 PostCreateDTO, PostReportCreateDTO 를 생성하고,
    각 DTO 를 통해 Post 데이터와 Report 데이터를 생성해 연결합니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostReportCreateRequestDTO{

        private String title;

        private String content;

        private String location;

        private String reportImage;

        private LocalDateTime date;

        private int category;

        //보고서에 그룹을 연결합니다
        private long groupId;

        private PostReportGroupType type;


    }



    /*
    보고서 게시글을 생성하고 반환하는 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostReportCreateResponseDTO{
        private Long postId;

        private Long authorId;

        private GroupDTO.GroupInfoDTO groupInfoDTO;

        private String title;

        private String content;

        private String location;

        private String reportImage;

        private LocalDateTime date;

        private LocalDateTime createdAt;

        private int category;

        private PostReportGroupType type;

        @Builder
        public PostReportCreateResponseDTO(Long postId,
                                           Long authorId,
                                           GroupDTO.GroupInfoDTO groupInfoDTO,
                                           String title,
                                           String content,
                                           String location,
                                           String reportImage,
                                           LocalDateTime date,
                                           LocalDateTime createdAt,
                                           int category,
                                           PostReportGroupType type) {
            this.postId = postId;
            this.authorId = authorId;
            this.groupInfoDTO = groupInfoDTO;
            this.title = title;
            this.content = content;
            this.location = location;
            this.reportImage = reportImage;
            this.date = date;
            this.createdAt = createdAt;
            this.category = category;
            this.type = type;

        }
    }

    /*
    Post{...}CreateRequestDTO 에서 Post 를 생성할 정보만 빼내어 Post 를 생성하기 위한 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostCreateDTO{
        private Long authorId;

        private String title;

        private String content;

        private int category;

        @Builder
        public PostCreateDTO( Long authorId, String title, String content, int category ){
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.category = category;

        }

    }

    @Getter
    @NoArgsConstructor
    public static class ReportCreateDTO{
        private Long postId;

        private String location;

        private String reportImage;

        private LocalDateTime date;

        private long groupId;

        private PostReportGroupType type;

        @Builder
        public ReportCreateDTO(Long postId,String location, String reportImage, LocalDateTime date, long groupId, PostReportGroupType type) {
            this.postId = postId;
            this.location = location;
            this.reportImage = reportImage;
            this.date = date;
            this.groupId = groupId;
            this.type = type;
        }
    }

    /*
    보고서 게시물을 수정하기 위해 유저쪽에서 보내는 DTO 입니다.
    CreateRequest 와 마찬가지로 Service 계층에서 PostUpdateDTO 와  PostReportUpdateDTO 를 분리해서 사용합니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostReportUpdateRequestDTO{

        private String title;

        private String content;

        private String location;

        private String reportImage;

        private LocalDateTime date;

        private long groupId;

        private PostReportGroupType type;

    }

    /*
    Post{...}UpdateRequestDTO 에서 Post 를 Update 데이터만 추출하여 사용하기 위한  DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostUpdateDTO{

        private String title;

        private String content;

        @Builder
        public PostUpdateDTO(String title, String content){
            this.title = title;
            this.content = content;
        }
    }

    /*
    PostReportUpdateDTO 에서 Report 데이터를 Update 시킲 데이터만 추출해서 사용하기 위한 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class ReportUpdateDTO{

        private String location;

        private String reportImage;

        private LocalDateTime date;

        private long groupId;

        private PostReportGroupType type;

        @Builder
        public ReportUpdateDTO(String location, String reportImage, LocalDateTime date, long groupId, PostReportGroupType type) {
            this.location = location;
            this.reportImage = reportImage;
            this.date = date;
            this.groupId = groupId;
            this.type = type;
        }
    }

    //--------------------------PROJECT 관련 DTO---------------------//
    /*
    Project 게시글 상세 조회에 사용되는 DTO
    */
    @Getter
    @NoArgsConstructor
    public static class PostProjectCreateRequestDTO{

        private String title;
        private String content;
        @Schema(
                description = "모집 분야 (복수 입력 가능, 대문자 영어만 허용)",
                example = "[\"BACKEND\", \"FRONTEND\"]",
                allowableValues = {"BACKEND","FRONTEND","DEV","PLANNING","DESIGN","ETC"}
        )
        private List<String> recruitmentFields;
        private boolean recruiting;
        private int category;
    }

    @Getter
    @NoArgsConstructor
    public static class PostProjectCreateResponseDTO{
        private Long postId;
        private Long authorId;
        private String title;
        private String content;
        private List<String> recruitmentFields;
        private boolean recruiting;
        private LocalDateTime createdAt;
        private int category;

        @Builder
        public PostProjectCreateResponseDTO(Long postId,
                                            Long authorId,
                                            String title,
                                            String content,
                                            List<String> recruitmentFields,
                                            boolean recruiting,
                                            LocalDateTime createdAt,
                                            int category){
            this.postId = postId;
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.recruitmentFields = recruitmentFields;
            this.recruiting = recruiting;
            this.createdAt = createdAt;
            this.category = category;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ProjectCreateDTO{
        private Long postId;
        private List<String> recruitmentFields;
        private boolean recruiting;

        @Builder
        public ProjectCreateDTO(Long postId,List<String> recruitmentFields, boolean recruiting){
            this.postId = postId;
            this.recruitmentFields = recruitmentFields;
            this.recruiting = recruiting;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PostProjectUpdateRequestDTO{
        private String title;
        private String content;
        private List<String> recruitmentFields;
        private boolean recruiting;
    }

    @Getter
    @NoArgsConstructor
    public static class ProjectUpdateDTO{

        private List<String> recruitmentFields;
        private boolean recruiting;

        @Builder
        public ProjectUpdateDTO(List<String> recruitmentFields, boolean recruiting){
            this.recruitmentFields = recruitmentFields;
            this.recruiting = recruiting;
        }
    }

    /*
    Project 게시글 목록 조회에 사용되는 DTO
    제목, 모집분야, 작성자, 생성시간, 모집여부를 포함합니다
     */
    @Getter
    @NoArgsConstructor
    public static class ProjectInfoDetailDTO {
        private Long postId;
        private String title;
        private String content;
        private List<String> recruitmentFields;
        private Long authorId;
        private LocalDateTime createdAt;
        private boolean recruiting;

        @Builder
        public ProjectInfoDetailDTO(Long postId, String title, String content, List<String> recruitmentFields, Long authorId, LocalDateTime createdAt, boolean recruiting) {
            this.postId = postId;
            this.title = title;
            this.content=content;
            this.recruitmentFields = recruitmentFields;
            this.authorId = authorId;
            this.createdAt = createdAt;
            this.recruiting = recruiting;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ProjectListDTO {
        private Long postId;
        private String title;
        private List<String> recruitmentFields;
        private Long authorId;
        private LocalDateTime createdAt;
        private boolean recruiting;

        @Builder
        public ProjectListDTO(Long postId, String title,List<String> recruitmentFields, Long authorId, LocalDateTime createdAt, boolean recruiting) {
            this.postId = postId;
            this.title = title;
            this.recruitmentFields = recruitmentFields;
            this.authorId = authorId;
            this.createdAt = createdAt;
            this.recruiting = recruiting;
        }
    }

    //--------------------------STUDY 관련 DTO---------------------//
    /*
    Study(스터디 모집) 게시글 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 모집 게시글 생성 요청 DTO")
    public static class PostStudyCreateRequestDTO {
        @Schema(description = "스터디 게시글 제목")
        private String title;
        @Schema(description = "스터디 게시글 내용")
        private String content;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름 (마감 시 그룹 이름으로 사용)", example = "Spring 스터디")
        private String studyName;
        @Schema(description = "모집 중 여부 (true=모집 중, false=모집 완료)")
        private boolean recruiting;
        @Schema(description = "최대 모집 인원 (팀장 포함)")
        private int maxMembers;
        @Schema(description = "게시글 카테고리 코드")
        private int category;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 모집 게시글 생성 응답 DTO")
    public static class PostStudyCreateResponseDTO {
        @Schema(description = "생성된 게시글 ID")
        private Long postId;
        @Schema(description = "작성자 회원 ID")
        private Long authorId;
        @Schema(description = "스터디 게시글 제목")
        private String title;
        @Schema(description = "스터디 게시글 내용")
        private String content;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름")
        private String studyName;
        @Schema(description = "모집 중 여부")
        private boolean recruiting;
        @Schema(description = "최대 모집 인원 (팀장 포함)")
        private int maxMembers;
        @Schema(description = "게시글 생성 시각")
        private LocalDateTime createdAt;
        @Schema(description = "게시글 카테고리 코드")
        private int category;

        @Builder
        public PostStudyCreateResponseDTO(Long postId, Long authorId, String title, String content,
                                          List<String> studyTags, String studyName, boolean recruiting,
                                          int maxMembers, LocalDateTime createdAt, int category) {
            this.postId = postId;
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.studyTags = studyTags;
            this.studyName = studyName;
            this.recruiting = recruiting;
            this.maxMembers = maxMembers;
            this.createdAt = createdAt;
            this.category = category;
        }
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 서브 테이블 생성용 내부 DTO")
    public static class StudyCreateDTO {
        @Schema(description = "연결된 Post ID")
        private Long postId;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름")
        private String studyName;
        @Schema(description = "모집 중 여부")
        private boolean recruiting;
        @Schema(description = "최대 모집 인원 (팀장 포함)")
        private int maxMembers;

        @Builder
        public StudyCreateDTO(Long postId, List<String> studyTags, String studyName, boolean recruiting, int maxMembers) {
            this.postId = postId;
            this.studyTags = studyTags;
            this.studyName = studyName;
            this.recruiting = recruiting;
            this.maxMembers = maxMembers;
        }
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 게시글 수정 요청 DTO")
    public static class PostStudyUpdateRequestDTO {
        @Schema(description = "수정할 제목")
        private String title;
        @Schema(description = "수정할 내용")
        private String content;
        @Schema(description = "수정할 태그 목록")
        private List<String> studyTags;
        @Schema(description = "모집 중 여부")
        private boolean recruiting;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "Study 엔티티 수정용 내부 DTO")
    public static class StudyUpdateDTO {
        @Schema(description = "수정할 태그 목록")
        private List<String> studyTags;
        @Schema(description = "모집 중 여부")
        private boolean recruiting;

        @Builder
        public StudyUpdateDTO(List<String> studyTags, boolean recruiting) {
            this.studyTags = studyTags;
            this.recruiting = recruiting;
        }
    }

    /*
    Study 게시글 상세 조회 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 게시글 상세 조회 DTO")
    public static class StudyInfoDetailDTO {
        @Schema(description = "게시글 ID")
        private Long postId;
        @Schema(description = "게시글 제목")
        private String title;
        @Schema(description = "게시글 내용")
        private String content;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름")
        private String studyName;
        @Schema(description = "작성자 회원 ID")
        private Long authorId;
        @Schema(description = "게시글 생성 시각")
        private LocalDateTime createdAt;
        @Schema(description = "모집 중 여부")
        private boolean recruiting;
        @Schema(description = "최대 모집 인원 (팀장 포함)")
        private int maxMembers;

        @Builder
        public StudyInfoDetailDTO(Long postId, String title, String content, List<String> studyTags,
                                  String studyName, Long authorId, LocalDateTime createdAt,
                                  boolean recruiting, int maxMembers) {
            this.postId = postId;
            this.title = title;
            this.content = content;
            this.studyTags = studyTags;
            this.studyName = studyName;
            this.authorId = authorId;
            this.createdAt = createdAt;
            this.recruiting = recruiting;
            this.maxMembers = maxMembers;
        }
    }

    /*
    Study 게시글 목록 조회 DTO
    제목, 태그, 작성자, 생성시간, 모집여부를 포함합니다
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 게시글 목록 조회 DTO")
    public static class StudyListDTO {
        @Schema(description = "게시글 ID")
        private Long postId;
        @Schema(description = "게시글 제목")
        private String title;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름")
        private String studyName;
        @Schema(description = "작성자 회원 ID")
        private Long authorId;
        @Schema(description = "게시글 생성 시각")
        private LocalDateTime createdAt;
        @Schema(description = "모집 중 여부")
        private boolean recruiting;
        @Schema(description = "최대 모집 인원 (팀장 포함)")
        private int maxMembers;

        @Builder
        public StudyListDTO(Long postId, String title, List<String> studyTags, String studyName,
                            Long authorId, LocalDateTime createdAt, boolean recruiting, int maxMembers) {
            this.postId = postId;
            this.title = title;
            this.studyTags = studyTags;
            this.studyName = studyName;
            this.authorId = authorId;
            this.createdAt = createdAt;
            this.recruiting = recruiting;
            this.maxMembers = maxMembers;
        }
    }

    //보고서 게시글 미리보기 DTO의 보고서 관련 내용을 담고있는 DTO입니다
    @Getter
    @NoArgsConstructor
    public static class ReportPreviewDTO{
        private PostReportGroupType type;
        private boolean isAccepted;

        @Builder
        public ReportPreviewDTO(PostReportGroupType type, boolean isAccepted){
            this.type = type;
            this.isAccepted = isAccepted;
        }
    }

    //보고서 게시글 미리보기입니다 보고서미리보기/게시글정보/그룹미리보기 를 담고있습니다
    @Getter
    @NoArgsConstructor
    public static class PostReportPreviewDTO{
        private ReportPreviewDTO reportPreviewDTO;
        private PostInfoDTO postInfoDTO;
        private GroupDTO.GroupPreviewDTO groupPreviewDTO;

        @Builder
        public PostReportPreviewDTO(ReportPreviewDTO reportPreviewDTO, PostInfoDTO postInfoDTO, GroupDTO.GroupPreviewDTO groupPreviewDTO){
            this.reportPreviewDTO = reportPreviewDTO;
            this.postInfoDTO = postInfoDTO;
            this.groupPreviewDTO=groupPreviewDTO;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PostReportViewDTO{
        private PostInfoDTO postInfoDTO;
        private ReportInfoDTO reportInfoDTO;

        @Builder
        public PostReportViewDTO(PostInfoDTO postInfoDTO, ReportInfoDTO reportInfoDTO){
            this.postInfoDTO = postInfoDTO;
            this.reportInfoDTO = reportInfoDTO;
        }

    }



}
