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
    MainPost 객체의 정보를 담는 DTO 입니다.
    게시물 목록 형태로도 반환되고, 게시물의 내용을 자세히 확인할 때에도 해당 DTO 를 가져와 MainPost 의 내용을 반환합니다
     */
    @Getter
    @NoArgsConstructor
    public static class MainPostDTO {
        private Long postId;

        private Long authorId;

        private String title;

        private String content;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;
        @Builder
        public MainPostDTO(Long postId,
                           Long authorId,
                           String title,
                           String content,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
            this.authorId = authorId;
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;


        }
    }


    @NoArgsConstructor
    @Getter
    public static class MainPostCreateDTO {

        private String title;

        private String content;

        private int category;

    }


    @NoArgsConstructor
    @Getter
    public static class PostReportCreateDTO {

        private String location;

        private LocalDateTime date;

        private String startImage;

        private String endImage;
    }

    /*
    보고서 포스트의 보고서 부분의 내용을 담당하는 DTO 입니다
     */
    @Getter
    @NoArgsConstructor
    public static class PostReportDTO {


        private String location;

        private LocalDateTime date;

        private String startImage;

        private String endImage;

        @Builder
        public PostReportDTO(String location, LocalDateTime date, String startImage, String endImage) {
            this.location = location;
            this.date = date;
            this.startImage = startImage;
            this.endImage = endImage;
        }



    }
}
