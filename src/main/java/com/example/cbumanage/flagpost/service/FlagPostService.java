package com.example.cbumanage.flagpost.service;

import com.example.cbumanage.flagpost.dto.FlagPostDTO;
import com.example.cbumanage.flagpost.entity.FlagPost;
import com.example.cbumanage.flagpost.repository.FlagPostRepository;
import com.example.cbumanage.flagpost.util.FlagPostMapper;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlagPostService {

    private final FlagPostRepository flagPostRepository;
    private final FlagPostMapper flagPostMapper;
    private final PostRepository postRepository;

    @Transactional
    public FlagPostDTO.FlagPostCreateResponse createFlagPost(Long postId, FlagPostDTO.FlagPostCreateRequest req, Long userId) {
        postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        if (flagPostRepository.existsByAuthorIdAndPostIdAndIsDeletedFalse(userId, postId)) {
            throw new BaseException(ErrorCode.DUPLICATE_RESOURCE);
        }
        FlagPost flagPost = FlagPost.create(userId, postId, req.content());
        FlagPost saved = flagPostRepository.save(flagPost);
        return flagPostMapper.toFlagPostCreateResponse(saved);
    }

    public Page<FlagPostDTO.FlagPostPreviewDTO> getFlagPostPreviews(Pageable pageable) {
        return flagPostRepository.findFlagPostPreviews(pageable);
    }

    public FlagPostDTO.FlagPostInfoDTO getFlagPostInfo(Long flagPostId) {
        FlagPost flagPost = flagPostRepository.findByIdAndIsDeletedFalse(flagPostId)
                .orElseThrow(() -> new EntityNotFoundException("FlagPost Not Found"));
        return flagPostMapper.toFlagPostInfoDTO(flagPost);
    }

    @Transactional
    public void resolveFlagsByPostId(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        flagPostRepository.softDeleteAllByPostId(postId);
    }
}
