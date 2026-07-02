package com.example.cbumanage.comment.util;

import com.example.cbumanage.comment.dto.CommentDTO;
import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    private UserRepository userRepository;

    @Autowired
    public CommentMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CommentDTO.CommentInfoDTO toCommentInfoDTO(Comment comment) {
        User user = userRepository.findById(comment.getUserId()).orElseThrow(() -> new EntityNotFoundException("user Not Found"));
        return new CommentDTO.CommentInfoDTO(
                comment.getId(),
                comment.getUserId(),
                user.getGeneration(),
                user.getName(),
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

    public CommentDTO.FreeBoardCommentResponse toFreeBoardCommentDTO(Comment comment) {
        if (comment.isAnonymous()) {
            return new CommentDTO.FreeBoardCommentAnonymousInfoDTO(
                    comment.getId(),
                    comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent(),
                    comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                    comment.getCreatedAt(),
                    true
            );
        }
        User user = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        return new CommentDTO.FreeBoardCommentInfoDTO(
                comment.getId(),
                comment.getUserId(),
                user.getGeneration(),
                user.getName(),
                comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getCreatedAt(),
                false
        );
    }
}