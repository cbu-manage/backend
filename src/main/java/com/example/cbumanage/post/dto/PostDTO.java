package com.example.cbumanage.post.dto;

import com.example.cbumanage.group.dto.GroupDTO;
import com.example.cbumanage.group.entity.enums.GroupRecruitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import com.example.cbumanage.report.entity.enums.PostReportGroupType;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
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
    @Schema(description = "포스트 메인테이블의 정보를 담는 DTO입니다")
    public record PostInfoDTO(
            Long postId,

            @Schema(description = "작성자의 이름입니다")
            String authorName,

            @Schema(description = "작성자의 기수 입니다")
            Long generation,

            Long authorId,

            String title,

            String content,

            LocalDateTime createdAt,

            LocalDateTime updatedAt
    ) {
    }


    /*
    Post-Report 의 핵심 내용을 가지고 있는 DTO 입니다.
    보고서 게시글 상세보기를 할때 해당 DTO 가 PostInfoDTO 와 함께 불러와 집니다
     */
    @Schema(description = "보고서 테이블의 정보를 담는 DTO입니다")
    public record ReportInfoDTO(
            String location,

            @Schema(description = "s3버킷 url을 통해 이미지를 보여줍니다")
            String reportImage,

            LocalDateTime date,

            //그룹의 정보를 담고 있습니다
            @Schema(description = "보고서를 작성한 그룹의 정보를 담고 있습니다")
            GroupDTO.GroupInfoDTO groupInfoDTO,

            @Schema(description = "보고서에 기록할 활동의 타입입니다", example = "STUDY / PROJECT / MENTORING")
            PostReportGroupType type,

            @Schema(description = "보고서의 승인 여부입니다")
            boolean isAccepted
    ) {
    }


    /*
    보고서 게시물을 만들때, 유저가 보내는 데이터를 담는 DTO 입니다.
    해당 DTO 를 Controller 에서 받아와, Service 에서 PostCreateDTO, PostReportCreateDTO 를 생성하고,
    각 DTO 를 통해 Post 데이터와 Report 데이터를 생성해 연결합니다
     */
    @Schema(description = "보고서 게시글을 생성하는 requestDTO 입니다. 포스트 메인테이블과 포스트-보고서 서브테이블의 정보를 한번에 생성합니다")
    public record PostReportCreateRequestDTO(

    String title,

    String content,

    String location,

    LocalDateTime date,

    @Schema(description = "s3버킷에 사진을 업로드 하고 반환받은 url을 넣습니다")
    String reportImage,

    @Schema(description = "카테고리 번호 (서버에서 7로 고정 처리되며 클라이언트 입력값은 무시됩니다)", example = "7", accessMode = Schema.AccessMode.READ_ONLY)
    int category,

    //보고서에 그룹을 연결합니다
    @Schema(description = "보고서와 연결될 그룹의 ID(글을 쓰고있는 활동의 그룹 ID)를 넣습니다")
    long groupId,

    @Schema(description = "보고서에 기록할 활동의 타입입니다", example = "STUDY / PROJECT / MENTORING")
    PostReportGroupType type
    )
        {}



    /*
    보고서 게시글을 생성하고 반환하는 DTO 입니다
     */
    public record PostReportCreateResponseDTO(
        Long postId,

        Long authorId,

        @Schema(description = "보고서에 추가된 group의 정보를 담는 DTO입니다")
        GroupDTO.GroupInfoDTO groupInfoDTO,

        String title,

        String content,

        String location,

        String reportImage,

        LocalDateTime date,

        LocalDateTime createdAt,

        int category,

        PostReportGroupType type
)

    {}

    /*
    Post{...}CreateRequestDTO 에서 Post 를 생성할 정보만 빼내어 Post 를 생성하기 위한 DTO 입니다
     */
    @Schema(description = "포스트 메인테이블을 만드는 DTO입니다.")
    public record PostCreateDTO(
        Long authorId,

        String title,

        String content,

        int category
    )
    {

    }

    @Schema(description = "보고서 서브 테이블을 생성하는 코드입니다. postReportCreateRequestDTO에서 분리됩니다")
    public record ReportCreateDTO(
        Long postId,

        String location,

        String reportImage,

        LocalDateTime date,

        long groupId,

        PostReportGroupType type
    ){}

    /*
    보고서 게시물을 수정하기 위해 유저쪽에서 보내는 DTO 입니다.
    CreateRequest 와 마찬가지로 Service 계층에서 PostUpdateDTO 와  PostReportUpdateDTO 를 분리해서 사용합니다
     */
    @Schema(description = "보고서 게시글을 수정하는 DTO입니다. 포스트 메인테이블과 보고서 서브테이블의 수정을 한번에 처리합니다 ")
    public record PostReportUpdateRequestDTO(

        String title,

        String content,

        String location,

        String reportImage,

        LocalDateTime date,

        long groupId,

        PostReportGroupType type

    ){}

    /*
    Post{...}UpdateRequestDTO 에서 Post 를 Update 데이터만 추출하여 사용하기 위한  DTO 입니다
     */

    @Schema(description = "포스트 메인테이블의 정보들을 수정하는 DTO입니다")
    public record PostUpdateDTO(

        String title,

        String content
    )
        {}

    /*
    PostReportUpdateDTO 에서 Report 데이터를 Update 시킲 데이터만 추출해서 사용하기 위한 DTO 입니다
     */
    @Schema(description = "보고서 서브 테이블을 수정하는 DTO입니다. PostReportUpdateRequestDTO에서 분리됩니다")
    public record ReportUpdateDTO(

        String location,

        String reportImage,

        LocalDateTime date,

        long groupId,

        PostReportGroupType type

    )
    {}


    //--------------------------PROJECT 관련 DTO---------------------//
    /*
    Project 게시글 상세 조회에 사용되는 DTO
    */
    @Schema(description = "프로젝트 게시글 생성 요청 데이터")
    public record PostProjectCreateRequestDTO(
        @Schema(description = "게시글 제목", example = "스프링부트 기반 커뮤니티 개발 프로젝트")
        @NotBlank(message = "제목은 필수 입력값입니다.")
        String title,

        @Schema(description = "게시글 상세 내용", example = "함께 협업하며 성장할 팀원을 모집합니다...")
        @NotBlank(message = "내용은 필수 입력값입니다.")
        String content,

        @Schema(
                description = "모집 분야 (복수 입력 가능)",
                example = "[\"BACKEND\", \"FRONTEND\"]",
                allowableValues = {"BACKEND", "FRONTEND", "DEV", "PLANNING", "DESIGN", "ETC"}
        )
        @NotEmpty(message = "최소 하나 이상의 모집 분야를 선택해야 합니다.")
        List<String> recruitmentFields,

        @Schema(description = "현재 모집 여부 (true: 모집중, false: 모집마감)", example = "true")
        Boolean recruiting,

        @Schema(description = "모집 마감 기한")
        @FutureOrPresent(message = "마감일은 과거일 수 없습니다.")
        LocalDate deadline,

        @Min(1)
        @Schema(description = "최대 모집 인원",example="10")
        Integer maxMembers,

        @Schema(description = "카테고리 번호 (서버에서 2로 고정 처리되며 클라이언트 입력값은 무시됩니다)", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
        int category
    ){}

    @Schema(description = "프로젝트 게시글 생성 응답 데이터")
    @Builder
    public record PostProjectCreateResponseDTO(
        @Schema(description = "생성된 포스트 ID", example = "101")
        Long postId,

        @Schema(description = "작성자 유저 ID", example = "15")
        Long authorId,

        @Schema(description = "자동 생성된 프로젝트 그룹 ID", example = "50")
        Long groupId,

        @Schema(description = "작성자 기수")
        Long authorGeneration,

        @Schema(description = "작성자 이름")
        String authorName,

        @Schema(description = "게시글 제목")
        String title,

        @Schema(description = "게시글 내용")
        String content,

        @Schema(description = "설정된 모집 분야")
        List<String> recruitmentFields,

        @Schema(description = "모집 여부")
        Boolean recruiting,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt,

        @Schema(description = "모집 마감 기한")
        LocalDate deadline,

        @Schema(description = "최대 모집 인원")
        Integer maxMembers,

        @Schema(description = "카테고리 번호")
        int category
    ){}


    @Builder
    public record ProjectCreateDTO(
        Long postId,
        List<String> recruitmentFields,
        Boolean recruiting,
        LocalDate deadline,
        int maxMembers
    ){}

    @Schema(description = "프로젝트 게시글 수정 요청 데이터")
    public record PostProjectUpdateRequestDTO(
        @Schema(description = "수정할 제목", example = "[수정] 스프링부트 프로젝트")
        @NotBlank(message = "제목은 필수 입력값입니다.")
        String title,

        @Schema(description = "수정할 내용", example = "프로젝트 내용이 변경되었습니다.")
        @NotBlank(message = "내용은 필수 입력값입니다.")
        String content,

        @Schema(description = "수정할 모집 분야", example = "[\"BACKEND\", \"DESIGN\"]")
        @NotEmpty(message = "최소 하나 이상의 모집 분야를 선택해야 합니다.")
        List<String> recruitmentFields,

        @Schema(description = "모집 여부 상태 변경", example = "false")
        Boolean recruiting,

        @Schema(description = "모집 마감 기한 변경")
        @FutureOrPresent(message = "마감일은 과거일 수 없습니다.")
        LocalDate deadline,

        @Min(1)
        @Schema(description = "최대 모집 인원 변경 (생략 시 기존 값 유지)")
        Integer maxMembers
    ){}

    @Builder
    public record ProjectUpdateDTO(
        @Schema(description = "모집 분야 변경")
        List<String> recruitmentFields,

        @Schema(description = "모집 여부 상태 변경")
        Boolean recruiting,

        @Schema(description = "모집 마감 기한 변경")
        LocalDate deadline,

        @Schema(description = "최대 모집 인원 변경")
        Integer maxMembers
    ){}

    /*
    Project 게시글 상제 조회에 사용되는 DTO
    제목, 모집분야, 작성자, 생성시간, 모집여부를 포함합니다
     */
    @Schema(description = "프로젝트 게시글 상세 정보 (조회 유저의 권한 정보 포함)")
    @Builder
    public record ProjectInfoDetailDTO(
        @Schema(description = "포스트 ID", example = "101")
        Long postId,

        @Schema(description = "게시글 제목")
        String title,

        @Schema(description = "게시글 상세 내용")
        String content,

        @Schema(description = "모집 분야 리스트")
        List<String> recruitmentFields,

        @Schema(description = "작성자 ID", example = "15")
        Long authorId,

        @Schema(description = "작성자 기수", example = "34")
        Long authorGeneration,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "연결된 그룹 ID", example = "50")
        Long groupId,

        @Schema(description = "조회한 유저가 해당 프로젝트의 팀장(작성자)인지 여부. " +
                "true일 경우: '신청 인원 확인' 버튼 노출", example = "false")
        boolean isLeader,

        @Schema(description = "조회한 유저의 가입 신청 상태. " +
                "1. true: 이미 신청함 -> '신청 취소하기' 버튼 노출 " +
                "2. false: 신청 이력 없음 -> '신청하기' 버튼 노출 " +
                "3. null: 이미 그룹 멤버(승인됨) -> '가입 완료' 표시(버튼 비활성)", example = "false")
        Boolean hasApplied,

        @Schema(description = "작성 일시")
        LocalDateTime createdAt,

        @Schema(description = "모집 여부 (모집 중:true, 모집 완료:false)", example = "true")
        Boolean recruiting,

        @Schema(description = "모집 마감 기한")
        LocalDate deadline,

        @Schema(description = "조회 수 ")
        Long viewCount,

        @Schema(description = "현재 활동 중인 멤버 수(팀장 포함). maxMember와 함께 상세 화면에서 예) 2/4 형태 표시용", example = "2")
        int activeMemberCount,

        @Schema(description = "최대 모집 인원(팀장 포함). activeMemberCount와 함께 상세 화면에서 예) 2/4 형태 표시용", example = "4")
        int maxMembers,

        @Schema(description = "그룹 모집 상태")
        GroupRecruitmentStatus groupRecruitmentStatus
    ){}

    @Schema(description = "프로젝트 목록 조회용 요약 데이터")
    @Builder
    public record ProjectListDTO (
        @Schema(description = "포스트 ID", example = "101")
        Long postId,

        @Schema(description = "게시글 제목")
        String title,

        @Schema(description = "게시글 내용")
        String content,

        @Schema(description = "모집 분야")
        List<String> recruitmentFields,

        @Schema(description = "작성자 ID")
        Long authorId,

        @Schema(description = "작성자 기수")
        Long authorGeneration,

        @Schema(description = "작성자 이름")
        String authorName,

        @Schema(description = "작성 일시")
        LocalDateTime createdAt,

        @Schema(description = "모집 여부 (모집 중:true, 모집 완료:false)", example = "true")
        Boolean recruiting,

        @Schema(description = "모집 마감 기한")
        LocalDate deadline,

        @Schema(description = "조회 수 ")
        Long viewCount,

        @Schema(description = "현재 활동 중인 멤버 수(팀장 포함). maxMember와 함께 목록에서 예) 2/4 형태 표시용", example = "2")
        int activeMemberCount,

        @Schema(description = "최대 모집 인원(팀장 포함). activeMemberCount와 함께 목록에서 예) 2/4 형태 표시용", example = "4")
        int maxMembers
    ){}

    //보고서 게시글 미리보기입니다 보고서미리보기/게시글정보/그룹미리보기 를 담고있습니다
    //--------------------------STUDY 관련 DTO---------------------//
    /*
    Study(스터디 모집) 게시글 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 모집 게시글 생성 요청 DTO")
    public static class PostStudyCreateRequestDTO {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
        @Schema(description = "스터디 게시글 제목", example = "Spring Boot 심화 스터디 모집")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        @Schema(description = "스터디 게시글 내용", example = "함께 Spring Boot를 공부할 팀원을 모집합니다...")
        private String content;

        @Size(max = 10, message = "태그는 최대 10개까지 가능합니다.")
        @Schema(description = "스터디 태그 목록 (자유 입력)", example = "[\"Spring\", \"Java\", \"Backend\"]")
        private List<String> studyTags;

        @NotBlank(message = "스터디 이름은 필수입니다.")
        @Size(max = 50, message = "스터디 이름은 50자 이내여야 합니다.")
        @Schema(description = "스터디 이름 (마감 시 그룹 이름으로 사용)", example = "Spring 스터디")
        private String studyName;

        @Schema(description = "모집 중 여부 (true: 모집 중, false: 모집 마감)", example = "true")
        private Boolean recruiting;

        @Min(value = 2, message = "최대 인원은 팀장 포함 최소 2명 이상이어야 합니다.")
        @Max(value = 50, message = "최대 인원은 50명을 초과할 수 없습니다.")
        @Schema(description = "최대 모집 인원 (팀장 포함)", example = "5")
        private int maxMembers;

        @Schema(description = "카테고리 번호 (서버에서 1로 고정 처리되며 클라이언트 입력값은 무시됩니다)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        private int category;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 모집 게시글 생성 응답 DTO")
    public static class PostStudyCreateResponseDTO {
        @Schema(description = "생성된 게시글 ID", example = "101")
        private Long postId;
        @Schema(description = "작성자 회원 ID", example = "15")
        private Long authorId;
        @Schema(description = "자동 생성된 스터디 그룹 ID", example = "50")
        private Long groupId;
        @Schema(description = "작성자 기수")
        private Long authorGeneration;
        @Schema(description = "작성자 이름")
        private String authorName;
        @Schema(description = "스터디 게시글 제목")
        private String title;
        @Schema(description = "스터디 게시글 내용")
        private String content;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름")
        private String studyName;
        @Schema(description = "모집 중 여부 (true: 모집 중, false: 모집 마감)")
        private boolean recruiting;
        @Schema(description = "최대 모집 인원 (팀장 포함)", example = "5")
        private int maxMembers;
        @Schema(description = "게시글 생성 시각")
        private LocalDateTime createdAt;
        @Schema(description = "게시글 카테고리 번호", example = "1")
        private int category;

        @Builder
        public PostStudyCreateResponseDTO(Long postId, Long authorId, Long groupId,
                                          Long authorGeneration, String authorName,
                                          String title, String content,
                                          List<String> studyTags, String studyName, boolean recruiting,
                                          int maxMembers, LocalDateTime createdAt, int category) {
            this.postId = postId;
            this.authorId = authorId;
            this.groupId = groupId;
            this.authorGeneration = authorGeneration;
            this.authorName = authorName;
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
        @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
        @Schema(description = "수정할 제목", example = "[수정] Spring Boot 스터디")
        private String title;

        @Schema(description = "수정할 내용", example = "스터디 내용이 변경되었습니다.")
        private String content;

        @Size(max = 10, message = "태그는 최대 10개까지 가능합니다.")
        @Schema(description = "수정할 태그 목록", example = "[\"Spring\", \"JPA\"]")
        private List<String> studyTags;

        @Size(max = 50, message = "스터디 이름은 50자 이내여야 합니다.")
        @Schema(description = "수정할 스터디 이름", example = "Spring 심화 스터디")
        private String studyName;

        @Min(value = 2, message = "최대 인원은 팀장 포함 최소 2명 이상이어야 합니다.")
        @Max(value = 50, message = "최대 인원은 50명을 초과할 수 없습니다.")
        @Schema(description = "수정할 최대 모집 인원 (팀장 포함)", example = "6")
        private Integer maxMembers;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "Study 엔티티 수정용 내부 DTO")
    public static class StudyUpdateDTO {
        @Schema(description = "수정할 태그 목록")
        private List<String> studyTags;

        @Schema(description = "수정할 스터디 이름")
        private String studyName;

        @Schema(description = "수정할 최대 모집 인원")
        private Integer maxMembers;

        @Builder
        public StudyUpdateDTO(List<String> studyTags, String studyName, Integer maxMembers) {
            this.studyTags = studyTags;
            this.studyName = studyName;
            this.maxMembers = maxMembers;
        }
    }

    /*
    Study 게시글 상세 조회 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "스터디 게시글 상세 조회 DTO")
    public static class StudyInfoDetailDTO {
        @Schema(description = "게시글 ID", example = "101")
        private Long postId;
        @Schema(description = "게시글 제목")
        private String title;
        @Schema(description = "게시글 내용")
        private String content;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름")
        private String studyName;
        @Schema(description = "작성자(팀장) 회원 ID", example = "15")
        private Long authorId;
        @Schema(description = "작성자 기수", example = "34")
        private Long authorGeneration;
        @Schema(description = "작성자 이름", example = "홍길동")
        private String authorName;
        @Schema(description = "게시글 생성 시각")
        private LocalDateTime createdAt;
        @Schema(description = "모집 여부 (모집 중: true, 모집 마감: false)", example = "true")
        private boolean recruiting;
        @Schema(description = "현재 활동 중인 멤버 수 (팀장 포함). maxMembers와 함께 상세 화면에서 예) 2/4 형태 표시용", example = "2")
        private int activeMemberCount;
        @Schema(description = "최대 모집 인원 (팀장 포함). activeMemberCount와 함께 상세 화면에서 예) 2/4 형태 표시용", example = "5")
        private int maxMembers;

        @Schema(description = "마감 후 생성된 그룹 ID (모집 중이면 null)", example = "21")
        private Long groupId;

        @Schema(description = "조회한 유저가 팀장(작성자)인지 여부. true일 경우: '신청 인원 확인' 버튼 노출", example = "false")
        private boolean isLeader;

        @Schema(description = "조회한 유저의 신청 상태. " +
                "1. true: 이미 신청함(PENDING) → '신청 취소하기' 버튼 노출 " +
                "2. false: 신청 이력 없음 또는 비로그인 → '신청하기' 버튼 노출 " +
                "3. null: 이미 그룹 멤버(승인됨) → '가입 완료' 표시(버튼 비활성)", example = "false")
        private Boolean hasApplied;

        @Schema(description = "게시글 조회수", example = "42")
        private Long viewCount;

        @Builder
        public StudyInfoDetailDTO(Long postId, String title, String content, List<String> studyTags,
                                  String studyName, Long authorId, Long authorGeneration, String authorName,
                                  LocalDateTime createdAt,
                                  boolean recruiting, int activeMemberCount, int maxMembers, Long groupId,
                                  boolean isLeader, Boolean hasApplied, Long viewCount) {
            this.postId = postId;
            this.title = title;
            this.content = content;
            this.studyTags = studyTags;
            this.studyName = studyName;
            this.authorId = authorId;
            this.authorGeneration = authorGeneration;
            this.authorName = authorName;
            this.createdAt = createdAt;
            this.recruiting = recruiting;
            this.activeMemberCount = activeMemberCount;
            this.maxMembers = maxMembers;
            this.groupId = groupId;
            this.isLeader = isLeader;
            this.hasApplied = hasApplied;
            this.viewCount = viewCount;
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
        @Schema(description = "게시글 ID", example = "101")
        private Long postId;
        @Schema(description = "게시글 제목")
        private String title;
        @Schema(description = "스터디 태그 목록")
        private List<String> studyTags;
        @Schema(description = "스터디 이름")
        private String studyName;
        @Schema(description = "작성자 회원 ID", example = "15")
        private Long authorId;
        @Schema(description = "작성자 기수")
        private Long authorGeneration;
        @Schema(description = "작성자 이름")
        private String authorName;
        @Schema(description = "게시글 생성 시각")
        private LocalDateTime createdAt;
        @Schema(description = "모집 여부 (모집 중: true, 모집 마감: false)", example = "true")
        private boolean recruiting;
        @Schema(description = "현재 활동 중인 멤버 수 (팀장 포함). maxMembers와 함께 목록에서 예) 2/4 형태 표시용", example = "2")
        private int activeMemberCount;
        @Schema(description = "최대 모집 인원 (팀장 포함). activeMemberCount와 함께 목록에서 예) 2/4 형태 표시용", example = "5")
        private int maxMembers;
        @Schema(description = "조회수", example = "1")
        private Long viewCount;

        @Builder
        public StudyListDTO(Long postId, String title, List<String> studyTags, String studyName,
                            Long authorId, Long authorGeneration, String authorName,
                            LocalDateTime createdAt, boolean recruiting, int activeMemberCount, int maxMembers,
                            Long viewCount) {
            this.postId = postId;
            this.title = title;
            this.studyTags = studyTags;
            this.studyName = studyName;
            this.authorId = authorId;
            this.authorGeneration = authorGeneration;
            this.authorName = authorName;
            this.createdAt = createdAt;
            this.recruiting = recruiting;
            this.activeMemberCount = activeMemberCount;
            this.maxMembers = maxMembers;
            this.viewCount = viewCount;
        }
    }

    //보고서 게시글 미리보기 DTO의 보고서 관련 내용을 담고있는 DTO입니다
    @Schema(description = "보고서 목록에서 보고서 게시글을 미리보기 하기위한 DTO입니다")
    public record PostReportPreviewDTO(
        Long postId,
        String title,
        LocalDateTime createdAt,
        Long authorId,
        String authorName,

        PostReportGroupType type,
        @Schema(description = "보고서 승인 여부 입니다, 보고서 게시글이 생셩될때 기본값은 false로 생성되며, 운영진이 승인할 경우 True로 변겯됩니다")
        boolean isAccepted,

        @Schema(description = "보고서를 작성한 그룹의 ID입니다")
        Long groupId,
        @Schema(description = "보고서를 작성한 그룹의 이름입니다")
        String groupName,
        @Schema(description = "그룹의 활동인원 (status가 ACTIVE인 인원)의 수를 표기합니다")
        Long groupMemberCount
    ) {}

    @Schema(description = "보고서 게시글을 단건조회 할때 포스트+보고서의 정보를 종합적으로 담은 게시글 입니다")
    public record PostReportViewDTO(
        PostInfoDTO postInfoDTO,
        ReportInfoDTO reportInfoDTO
    ){}

    @Getter
    @NoArgsConstructor
    @Schema(description = "내 글 읽어오기에서 카테고리 입력하지 않을 시 보여줄 핵심내용입니다")
    public static class PostMyPageViewDTO{
        private Long postId;
        private String title;
        private String content;
        private int category;
        private LocalDateTime createdAt;

        private Long authorId;
        private String authorName;
        private Long authorGeneration;

        private Long viewCount;
        private Long commentCount;

        @Builder
        public PostMyPageViewDTO(Long postId, String title,String content ,int category, LocalDateTime createdAt, Long authorId, String authorName, Long authorGeneration, Long viewCount, Long commentCount) {
            this.postId = postId;
            this.title = title;
            this.content = content;
            this.category = category;
            this.createdAt = createdAt;
            this.authorId = authorId;
            this.authorName = authorName;
            this.authorGeneration = authorGeneration;
            this.viewCount = viewCount;
            this.commentCount = commentCount;
        }

    }





}
