package com.example.cbumanage.flagcomment.util;

import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.flagcomment.entity.FlagComment;
import com.example.cbumanage.flagcomment.dto.CommentDTO;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlagCommentMapper {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentDTO.FlagCommentCreateResponse toFlagCommentCreateResponse(FlagComment flagComment) {
        return new CommentDTO.FlagCommentCreateResponse(
                flagComment.getId(),
                flagComment.getCommentId(),
                flagComment.getAuthorId(),
                flagComment.getContent(),
                flagComment.getCreatedAt()
        );
    }

    public CommentDTO.FlagCommentInfoDTO toFlagCommentInfoDTO(FlagComment flagComment) {
        Comment targetComment = commentRepository.findById(flagComment.getCommentId())
                .orElseThrow(() -> new EntityNotFoundException("Comment Not Found"));
        User targetUser = userRepository.findById(targetComment.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Target User Not Found"));
        User author = userRepository.findById(flagComment.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author Not Found"));

        return new CommentDTO.FlagCommentInfoDTO(
                flagComment.getId(),
                flagComment.getContent(),
                flagComment.getCreatedAt(),
                targetComment.getId(),
                targetComment.getContent(),
                targetUser.getUserId(),
                targetUser.getName(),
                targetUser.getGeneration(),
                author.getUserId(),
                author.getName(),
                author.getGeneration()
        );
    }
}
