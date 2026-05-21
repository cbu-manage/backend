package com.example.cbumanage.flagcomment.service;

import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.flagcomment.dto.CommentDTO;
import com.example.cbumanage.flagcomment.entity.FlagComment;
import com.example.cbumanage.flagcomment.repository.FlagCommentRepository;
import com.example.cbumanage.flagcomment.util.FlagCommentMapper;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlagCommentService {

    private final FlagCommentRepository flagCommentRepository;
    private final FlagCommentMapper flagCommentMapper;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentDTO.FlagCommentCreateResponse createFlagComment(Long commentId, CommentDTO.FlagCommentCreateRequest req, Long userId) {
        commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment Not Found"));
        if (flagCommentRepository.existsByAuthorIdAndCommentIdAndIsDeletedFalse(userId, commentId)) {
            throw new BaseException(ErrorCode.DUPLICATE_RESOURCE);
        }
        FlagComment flagComment = FlagComment.create(userId, commentId, req.content());
        FlagComment saved = flagCommentRepository.save(flagComment);
        return flagCommentMapper.toFlagCommentCreateResponse(saved);
    }

    public Page<CommentDTO.FlagCommentPreviewDTO> getFlagCommentPreviews(Pageable pageable) {
        return flagCommentRepository.findFlagCommentPreviews(pageable);
    }

    public CommentDTO.FlagCommentInfoDTO getFlagCommentInfo(Long flagCommentId) {
        FlagComment flagComment = flagCommentRepository.findByIdAndIsDeletedFalse(flagCommentId)
                .orElseThrow(() -> new EntityNotFoundException("FlagComment Not Found"));
        return flagCommentMapper.toFlagCommentInfoDTO(flagComment);
    }

    @Transactional
    public void resolveFlagsByCommentId(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment Not Found"));
        flagCommentRepository.softDeleteAllByCommentId(commentId);
    }
}
