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

    public CommentDTO.CommentInfoDTO toCommentInfoDTO(Comment comment) {
        CbuMember cbuMember = cbuMemberRepository.findById(comment.getUserId()).orElseThrow(() -> new EntityNotFoundException("user Not Found"));
        return new CommentDTO.CommentInfoDTO(
                comment.getId(),
                comment.getUserId(),
                cbuMember.getGeneration(),
                cbuMember.getName(),
                comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public CommentDTO.CommentAnonymousInfoDTO toCommentAnonymousInfoDTO(Comment comment) {
        return new CommentDTO.CommentAnonymousInfoDTO(
                comment.getId(),
                comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent(),
                comment.getCreatedAt(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null
        );
    }

    public CommentDTO.CommentCreateResponseDTO toCommentCreateResponseDTO(Comment comment) {
        return new CommentDTO.CommentCreateResponseDTO(
                comment.getId(),
                comment.getUserId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    public CommentDTO.ReplyCreateResponseDTO toReplyCreateResponseDTO(Comment comment) {
        return new CommentDTO.ReplyCreateResponseDTO(
                comment.getId(),
                comment.getUserId(),
                comment.getParentComment().getId(),
                comment.getCreatedAt(),
                comment.getContent()
        );
    }
}
