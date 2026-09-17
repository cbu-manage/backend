package com.example.cbumanage.suggestion.service;

import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.suggestion.dto.SuggestionDTO;
import com.example.cbumanage.suggestion.entity.PostSuggestion;
import com.example.cbumanage.suggestion.entity.enums.SuggestionStatus;
import com.example.cbumanage.suggestion.entity.enums.SuggestionType;
import com.example.cbumanage.suggestion.repository.PostSuggestionRepository;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuggestionService {

    private final PostSuggestionRepository suggestionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostService postService;

    @Transactional
    public SuggestionDTO.SuggestionInfoDTO create(SuggestionDTO.SuggestionCreateRequest req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = new PostDTO.PostCreateDTO(
                userId, req.title(), req.content(), PostCategory.SUGGESTION.getValue()
        );
        Post post = postService.createPost(postCreateDTO);
        PostSuggestion suggestion = suggestionRepository.save(PostSuggestion.create(post, req.type()));
        return toInfo(suggestion, userId);
    }

    public Page<SuggestionDTO.SuggestionPreviewDTO> getList(
            Pageable pageable, SuggestionType type, SuggestionStatus status, Long userId) {
        Page<PostSuggestion> page;
        if (type != null && status != null) {
            page = suggestionRepository.findByTypeAndStatusAndPost_IsDeletedFalse(type, status, pageable);
        } else if (type != null) {
            page = suggestionRepository.findByTypeAndPost_IsDeletedFalse(type, pageable);
        } else if (status != null) {
            page = suggestionRepository.findByStatusAndPost_IsDeletedFalse(status, pageable);
        } else {
            page = suggestionRepository.findByPost_IsDeletedFalse(pageable);
        }
        return page.map(s -> toPreview(s, userId));
    }

    public SuggestionDTO.SuggestionSummaryDTO getSummary() {
        return new SuggestionDTO.SuggestionSummaryDTO(
                suggestionRepository.countByStatus(SuggestionStatus.OPEN),
                suggestionRepository.countByStatus(SuggestionStatus.RESOLVED)
        );
    }

    /** 단건 조회. 조회수는 여기서만 올린다 */
    @Transactional
    public SuggestionDTO.SuggestionInfoDTO get(Long postId, Long userId) {
        PostSuggestion suggestion = findActive(postId);
        suggestion.getPost().upViewCount();
        return toInfo(suggestion, userId);
    }

    /** 제목·내용·종류 수정 — 작성자 본인만 */
    @Transactional
    public void update(Long postId, SuggestionDTO.SuggestionUpdateRequest req, Long userId) {
        PostSuggestion suggestion = findActive(postId);
        Post post = suggestion.getPost();
        if (!post.getAuthorId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        postService.updatePost(new PostDTO.PostUpdateDTO(req.title(), req.content()), post);
        if (req.type() != null) {
            suggestion.changeType(req.type());
        }
    }

    /** 해결/미해결 전환 — 운영진 전부 */
    @Transactional
    public void updateStatus(Long postId, SuggestionStatus status, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        if (!user.getRole().isStaff()) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        findActive(postId).changeStatus(status);
    }

    /** 삭제는 PostService.softDeletePost(작성자 또는 관리자) 규칙을 그대로 쓴다 */
    @Transactional
    public void delete(Long postId, Long userId) {
        findActive(postId);
        postService.softDeletePost(postId, userId);
    }

    /**
     * 댓글·답글 작성. 무조건 익명. parentCommentId 가 있으면 같은 글의 살아 있는 댓글이어야 한다.
     * (CommentService 의 일반 댓글·답글 경로도 SUGGESTION 글이면 익명을 강제하므로 어느 길로 와도 같다)
     */
    @Transactional
    public SuggestionDTO.SuggestionCommentCreateResponse createComment(
            Long postId, SuggestionDTO.SuggestionCommentCreateRequest req, Long userId) {
        Post post = findActive(postId).getPost();
        Comment parent = null;
        if (req.parentCommentId() != null) {
            parent = commentRepository.findByIdAndIsDeletedFalse(req.parentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException("Comment Not Found"));
            if (!parent.getPost().getId().equals(postId)) {
                throw new BaseException(ErrorCode.INVALID_REQUEST);
            }
            // 답글에 답글은 받지 않는다 — 화면이 1단계 답글만 그린다
            if (parent.getParentComment() != null) {
                throw new BaseException(ErrorCode.INVALID_REQUEST);
            }
        }
        Comment comment = new Comment(post, userId, parent, req.content(), true);
        if (parent != null) {
            parent.addReply(comment);
        }
        Comment saved = commentRepository.save(comment);
        return new SuggestionDTO.SuggestionCommentCreateResponse(
                saved.getId(),
                postId,
                parent != null ? parent.getId() : null,
                saved.getCreatedAt()
        );
    }

    /** 댓글 목록(답글 포함, 작성 순). 지운 댓글은 자리만 남긴다 */
    public List<SuggestionDTO.SuggestionCommentDTO> getComments(Long postId, Long userId) {
        findActive(postId);
        return commentRepository.findByPostId(postId).stream()
                .map(c -> new SuggestionDTO.SuggestionCommentDTO(
                        c.getId(),
                        c.isDeleted() ? "삭제된 댓글입니다" : c.getContent(),
                        c.getParentComment() != null ? c.getParentComment().getId() : null,
                        c.getCreatedAt(),
                        c.isDeleted(),
                        !c.isDeleted() && userId.equals(c.getUserId())
                ))
                .toList();
    }

    private PostSuggestion findActive(Long postId) {
        return suggestionRepository.findActiveByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("Suggestion Not Found"));
    }

    private SuggestionDTO.SuggestionPreviewDTO toPreview(PostSuggestion s, Long userId) {
        Post post = s.getPost();
        return new SuggestionDTO.SuggestionPreviewDTO(
                post.getId(),
                post.getTitle(),
                s.getType(),
                s.getStatus(),
                post.getCreatedAt(),
                post.getViewCount(),
                commentRepository.countByPostId(post.getId()),
                post.getAuthorId().equals(userId)
        );
    }

    private SuggestionDTO.SuggestionInfoDTO toInfo(PostSuggestion s, Long userId) {
        Post post = s.getPost();
        return new SuggestionDTO.SuggestionInfoDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                s.getType(),
                s.getStatus(),
                post.getCreatedAt(),
                s.getResolvedAt(),
                post.getViewCount(),
                commentRepository.countByPostId(post.getId()),
                post.getAuthorId().equals(userId)
        );
    }
}
