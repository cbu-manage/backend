package com.example.cbumanage.comment.util;

import com.example.cbumanage.comment.dto.CommentDTO;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    private CbuMemberRepository cbuMemberRepository;

    @Autowired
    public CommentMapper(CbuMemberRepository cbuMemberRepository) {
        this.cbuMemberRepository = cbuMemberRepository;
    }

    /*
    CommentInfo를 만드는 Mapper입니다.
    댓글 엔티티의 replies를 toReplyInfoDTO를 통해 전부 DTO로 변환시킨후, CommentInfoDTO에 담아서 반환합니다

    softDelete - 만약 삭제된 댓글이지만 답글이 있을경우, 댓글-답글 목록은 보여주되, 내용은 삭제된 댓글입이다 라고 표시합니다
     */
    public CommentDTO.CommentInfoDTO toCommentInfoDTO(Comment comment) {
        CbuMember cbuMember = cbuMemberRepository.findById(comment.getUserId()).orElseThrow(()->new EntityNotFoundException("user Not Found"));
        return CommentDTO.CommentInfoDTO.builder()
                .commentId(comment.getId())
                .userId(comment.getUserId())
                .generation(cbuMember.getGeneration())
                .userName(cbuMember.getName())
                .content(comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent())
                .parentCommentId(comment.getParentComment()!=null?comment.getParentComment().getId():null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    // 삼항 연산자를 사용하여 comment 객체의 post 필드가 null이 아니면 postId를,
    // problem 필드가 null이 아니면 problemId를 DTO에 담도록 로직을 수정했습니다.
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
                .content(comment.getContent())
                .build();
    }
}
