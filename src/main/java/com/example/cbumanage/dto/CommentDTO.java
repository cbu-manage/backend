package com.example.cbumanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDTO {
    /*
    Comment의 정보를 담는 DTO입니다
    답글의 DTO를 함께 담고 있습니다.
     */

    @Getter
    @NoArgsConstructor
    public static class CommentInfoDTO{
        private Long id;

        private Long userId;

        private String content;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        @Schema(description = "답글 리스트")
        private List<ReplyInfoDTO>  replies;

        @Builder
        public CommentInfoDTO(Long id,
                              Long userId,
                              String content,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt,
                              List<ReplyInfoDTO> replies)
        {
            this.id = id;
            this.userId = userId;
            this.content = content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.replies = replies;
        }

    }
    /*
    댓글과 답글의 엔티티는 같지만, DTO는 다르게 관리합니다.
    답글의 DTO는 댓글의 DTO에 포함시켜서 반환합니다
     */

    @Getter
    @NoArgsConstructor
    public static class ReplyInfoDTO{
        private Long id;

        private Long userId;

        private String content;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        @Builder
        public ReplyInfoDTO(Long id, Long userId, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.userId = userId;
            this.content = content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

    }

    @Getter
    @NoArgsConstructor
    public static class CommentCreateRequestDTO{
        private String content;
    }
    @Getter
    @NoArgsConstructor
    public static class CommentCreateResponseDTO{
        private Long commentId;

        private Long userId;

        private Long postId;

        private String content;

        private LocalDateTime createdAt;

        @Builder
        public  CommentCreateResponseDTO(Long commentId,Long userId,Long postId,String content,LocalDateTime createdAt){
            this.commentId = commentId;
            this.userId = userId;
            this.postId = postId;
            this.content = content;
            this.createdAt = createdAt;
        }

    }

    @Getter
    @NoArgsConstructor
    public static class CommentUpdateRequestDTO{
        private String content;
    }

    @Getter
    @NoArgsConstructor
    public static class ReplyCreateRequestDTO{
        private String content;
    }

    @Getter
    @NoArgsConstructor
    public static class ReplyCreateResponseDTO{
        private Long replyId;

        private Long userId;

        private Long parentId;

        private LocalDateTime createdAt;

        private String content;

        @Builder
        public  ReplyCreateResponseDTO(Long replyId,Long userId,Long parentId,LocalDateTime createdAt,String content){
            this.replyId = replyId;
            this.userId = userId;
            this.parentId = parentId;
            this.createdAt = createdAt;
            this.content = content;
        }
    }




}
