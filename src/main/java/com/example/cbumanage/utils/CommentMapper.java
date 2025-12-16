package com.example.cbumanage.utils;

import com.example.cbumanage.dto.CommentDTO;
import com.example.cbumanage.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    /*
    CommentInfo를 만드는 Mapper입니다.
    댓글 엔티티의 replies를 toReplyInfoDTO를 통해 전부 DTO로 변환시킨후, CommentInfoDTO에 담아서 반환합니다

    softDelete - 만약 삭제된 댓글이지만 답글이 있을경우, 댓글-답글 목록은 보여주되, 내용은 삭제된 댓글입이다 라고 표시합니다
     */
    public CommentDTO.CommentInfoDTO toCommentInfoDTO(Comment comment) {
        return CommentDTO.CommentInfoDTO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .content(comment.isDeleted() ?"삭제된 댓글입니다" : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(comment.getReplies().stream().map(reply->toReplyInfoDTO(reply)).toList())
                .build();
    }

    /*
    Comment의 replies를 DTO로 변환시키기 위한 메소드 입니다

    softDelete - 만약 삭제된 답글이라면, 내용에 삭제된 답글이라고 표시합니다
     */
    public CommentDTO.ReplyInfoDTO toReplyInfoDTO(Comment comment) {
        return CommentDTO.ReplyInfoDTO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .content(comment.isDeleted() ? "삭제된 답글입니다" :  comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public CommentDTO.CommentCreateResponseDTO toCommentCreateResponseDTO(Comment comment) {
        return CommentDTO.CommentCreateResponseDTO.builder()
                .commentId(comment.getId())
                .userId(comment.getUserId())
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public CommentDTO.ReplyCreateResponseDTO toReplyCreateResponseDTO(Comment comment) {
        return CommentDTO.ReplyCreateResponseDTO.builder()
                .replyId(comment.getId())
                .userId(comment.getUserId())
                .parentId(comment.getParentComment().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
