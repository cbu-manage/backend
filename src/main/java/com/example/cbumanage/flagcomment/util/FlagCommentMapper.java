package com.example.cbumanage.flagcomment.util;

import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.flagcomment.entity.FlagComment;
import com.example.cbumanage.flagcomment.dto.CommentDTO;
import com.example.cbumanage.post.util.AnonymityResolver;
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
    private final AnonymityResolver anonymityResolver;

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
        User author = userRepository.findById(flagComment.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author Not Found"));

        // 익명 댓글은 작성자를 내려주지 않는다.
        User targetUser = anonymityResolver.isAnonymous(targetComment)
                ? null
                : userRepository.findById(targetComment.getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("Target User Not Found"));

        return new CommentDTO.FlagCommentInfoDTO(
                flagComment.getId(),
                flagComment.getContent(),
                flagComment.getCreatedAt(),
                targetComment.getId(),
                targetComment.getContent(),
                targetUser == null ? null : targetUser.getUserId(),
                targetUser == null ? null : targetUser.getName(),
                targetUser == null ? null : targetUser.getGeneration(),
                author.getUserId(),
                author.getName(),
                author.getGeneration()
        );
    }
}
