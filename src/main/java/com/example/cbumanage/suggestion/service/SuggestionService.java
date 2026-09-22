package com.example.cbumanage.suggestion.service;

import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.suggestion.dto.SuggestionDTO;
import com.example.cbumanage.suggestion.entity.PostSuggestion;
import com.example.cbumanage.suggestion.entity.enums.SuggestionStatus;
import com.example.cbumanage.suggestion.entity.enums.SuggestionType;
import com.example.cbumanage.suggestion.repository.PostSuggestionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 건의 게시판. 권한(운영진·ADMIN)은 컨트롤러 @PreAuthorize 가 유일한 판정 지점이고,
 * 여기서는 "작성자 본인" 규칙만 본다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuggestionService {

    private final PostSuggestionRepository suggestionRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final EntityManager entityManager;

    @Transactional
    public SuggestionDTO.SuggestionInfoDTO create(SuggestionDTO.SuggestionCreateRequest req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = new PostDTO.PostCreateDTO(
                userId, req.title(), req.content(), PostCategory.SUGGESTION.getValue()
        );
        Post post = postService.createPost(postCreateDTO);
        PostSuggestion suggestion = suggestionRepository.save(PostSuggestion.create(post, req.type()));
        return toInfo(suggestion, userId, 0L);
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
        return toPreviewPage(page, userId);
    }

    /** 마이페이지 — 내가 쓴 건의. 전부 본인 글이므로 isAuthor 는 항상 true */
    public Page<SuggestionDTO.SuggestionPreviewDTO> getMy(Pageable pageable, Long userId) {
        return toPreviewPage(suggestionRepository.findByPost_AuthorIdAndPost_IsDeletedFalse(userId, pageable), userId);
    }

    public SuggestionDTO.SuggestionSummaryDTO getSummary() {
        return new SuggestionDTO.SuggestionSummaryDTO(
                suggestionRepository.countByStatus(SuggestionStatus.OPEN),
                suggestionRepository.countByStatus(SuggestionStatus.RESOLVED)
        );
    }

    /** 단건 조회. 조회수는 atomic UPDATE 로 먼저 올리고, 그 뒤에 읽어 방금 올린 값이 응답에 실리게 한다 */
    @Transactional
    public SuggestionDTO.SuggestionInfoDTO get(Long postId, Long userId) {
        suggestionRepository.findActiveByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("Suggestion Not Found"));
        postRepository.incrementViewCount(postId);
        // 벌크 UPDATE 는 영속성 컨텍스트를 건드리지 않는다. 비우지 않으면 아래 재조회가 캐시를 읽어
        // 방금 올린 조회수가 응답에 안 실린다(NewsService 와 같은 처리).
        entityManager.clear();
        PostSuggestion suggestion = findActive(postId);
        return toInfo(suggestion, userId, commentRepository.countByPostId(postId));
    }

    /** 제목·내용·종류 수정 — 작성자 본인만. 넘긴 필드는 비어 있으면 안 된다(생성 규칙과 동일) */
    @Transactional
    public void update(Long postId, SuggestionDTO.SuggestionUpdateRequest req, Long userId) {
        if ((req.title() != null && req.title().isBlank())
                || (req.content() != null && req.content().isBlank())) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }
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

    /** 해결/미해결 전환 — 운영진(컨트롤러 @PreAuthorize) */
    @Transactional
    public void updateStatus(Long postId, SuggestionStatus status) {
        findActive(postId).changeStatus(status);
    }

    /** 상단 고정 토글 — ADMIN(컨트롤러 @PreAuthorize) */
    @Transactional
    public void updatePinned(Long postId, boolean pinned) {
        findActive(postId).pin(pinned);
    }

    /** 삭제는 PostService.softDeletePost(작성자 또는 관리자) 규칙을 그대로 쓴다 */
    @Transactional
    public void delete(Long postId, Long userId) {
        findActive(postId);
        postService.softDeletePost(postId, userId);
    }

    /**
     * 댓글·답글 작성. 무조건 익명. parentCommentId 가 있으면 같은 글의 살아 있는 댓글이어야 한다(답글의 답글도 자유게시판처럼 허용).
     * (CommentService 의 일반 댓글·답글·자유게시판 경로도 SUGGESTION 글이면 익명을 강제하므로 어느 길로 와도 같다)
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

    /** 한 페이지의 댓글 수를 GROUP BY 한 번으로 모아 붙인다 */
    private Page<SuggestionDTO.SuggestionPreviewDTO> toPreviewPage(Page<PostSuggestion> page, Long userId) {
        List<Long> postIds = page.getContent().stream().map(s -> s.getPost().getId()).toList();
        Map<Long, Long> counts = new HashMap<>();
        if (!postIds.isEmpty()) {
            for (Object[] row : commentRepository.countByPostIds(postIds)) {
                counts.put((Long) row[0], (Long) row[1]);
            }
        }
        return page.map(s -> toPreview(s, userId, counts.getOrDefault(s.getPost().getId(), 0L)));
    }

    private SuggestionDTO.SuggestionPreviewDTO toPreview(PostSuggestion s, Long userId, Long commentCount) {
        Post post = s.getPost();
        return new SuggestionDTO.SuggestionPreviewDTO(
                post.getId(),
                post.getTitle(),
                s.getType(),
                s.getStatus(),
                s.isPinned(),
                post.getCreatedAt(),
                post.getViewCount(),
                commentCount,
                post.getAuthorId().equals(userId)
        );
    }

    private SuggestionDTO.SuggestionInfoDTO toInfo(PostSuggestion s, Long userId, Long commentCount) {
        Post post = s.getPost();
        return new SuggestionDTO.SuggestionInfoDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                s.getType(),
                s.getStatus(),
                s.isPinned(),
                post.getCreatedAt(),
                s.getResolvedAt(),
                post.getViewCount(),
                commentCount,
                post.getAuthorId().equals(userId)
        );
    }
}
