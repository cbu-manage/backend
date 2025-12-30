package com.example.cbumanage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

        private String startImage;

        private String endImage;

        private LocalDateTime date;

        @Builder
        public ReportInfoDTO(String location, String startImage, String endImage, LocalDateTime date){
            this.location = location;
            this.startImage = startImage;
            this.endImage = endImage;
            this.date = date;

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

        private String startImage;

        private String endImage;

        private LocalDateTime date;

        private int category;


    }

    /*
    보고서 게시글을 생성하고 반환하는 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostReportCreateResponseDTO{
        private Long postId;

        private Long authorId;

        private String title;

        private String content;

        private String location;

        private String startImage;

        private String endImage;

        private LocalDateTime date;

        private LocalDateTime createdAt;

        private int category;

        @Builder
        public PostReportCreateResponseDTO(Long postId,
                                           Long authorId,
                                           String title,
                                           String content,
                                           String location,
                                           String startImage,
                                           String endImage,
                                           LocalDateTime date,
                                           LocalDateTime createdAt,
                                           int category){
            this.postId = postId;
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.location = location;
            this.startImage = startImage;
            this.endImage = endImage;
            this.date = date;
            this.createdAt = createdAt;
            this.category = category;

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


    /*
    PostCreateRequestDTO 에서 Report 를 생성할 데이터만을 뽑아와 Report 데이터를 생성하는데에 사용될 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class ReportCreateDTO{
        private Long postId;

        private String location;

        private String startImage;

        private String endImage;

        private LocalDateTime date;

        @Builder
        public ReportCreateDTO(Long postId,String location, String startImage, String endImage, LocalDateTime date){
            this.postId = postId;
            this.location = location;
            this.startImage = startImage;
            this.endImage = endImage;
            this.date = date;

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

        private String startImage;

        private String endImage;

        private LocalDateTime date;

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

        private String StartImage;

        private String endImage;

        private LocalDateTime date;

        @Builder
        public ReportUpdateDTO(String location, String StartImage, String endImage, LocalDateTime date){
            this.location = location;
            this.StartImage = StartImage;
            this.endImage = endImage;
            this.date = date;
        }
    }

    /*
    Post-Study 의 핵심 내용을 가지고 있는 DTO 입니다.
    스터디 모집 게시글 상세보기를 할때 해당 DTO 가 PostInfoDTO 와 함께 불러와 집니다
     */
    @Getter
    @NoArgsConstructor
    public static class StudyInfoDTO{

        private Boolean status;

        @Builder
        public StudyInfoDTO(Boolean status){
            this.status = status;
        }
    }

    /*
    스터디 모집 게시물을 만들때, 유저가 보내는 데이터를 담는 DTO 입니다.
    해당 DTO 를 Controller 에서 받아와, Service 에서 PostCreateDTO, PostStudyCreateDTO 를 생성하고,
    각 DTO 를 통해 Post 데이터와 Study 데이터를 생성해 연결합니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostStudyCreateRequestDTO{

        private String title;

        private String content;

        private Boolean status;

        private int category;

    }

    /*
    스터디 모집 게시글을 생성하고 반환하는 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostStudyCreateResponseDTO{
        private Long postId;

        private Long authorId;

        private String title;

        private String content;

        private Boolean status;

        private LocalDateTime createdAt;

        private int category;

        @Builder
        public PostStudyCreateResponseDTO(Long postId,
                                           Long authorId,
                                           String title,
                                           String content,
                                           Boolean status,
                                           LocalDateTime createdAt,
                                           int category){
            this.postId = postId;
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.status = status;
            this.createdAt = createdAt;
            this.category = category;
        }
    }

    /*
    PostCreateRequestDTO 에서 Study 를 생성할 데이터만을 뽑아와 Study 데이터를 생성하는데에 사용될 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class StudyCreateDTO{
        private Long postId;

        private Boolean status;

        @Builder
        public StudyCreateDTO(Long postId, Boolean status){
            this.postId = postId;
            this.status = status;
        }
    }

    /*
    스터디 모집 게시물을 수정하기 위해 유저쪽에서 보내는 DTO 입니다.
    CreateRequest 와 마찬가지로 Service 계층에서 PostUpdateDTO 와 PostStudyUpdateDTO 를 분리해서 사용합니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostStudyUpdateRequestDTO{

        private String title;

        private String content;

        private Boolean status;

    }

    /*
    PostStudyUpdateDTO 에서 Study 데이터를 Update 시킨 데이터만 추출해서 사용하기 위한 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class StudyUpdateDTO{

        private Boolean status;

        @Builder
        public StudyUpdateDTO(Boolean status){
            this.status = status;
        }
    }

}
