package com.example.cbumanage.comment.service;

import com.example.cbumanage.comment.dto.CommentDTO;
import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.freeboard.repository.PostFreeboardRepository;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.comment.util.CommentMapper;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;
    private final PostFreeboardRepository postFreeboardRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentDTO.CommentCreateResponseDTO createComment(CommentDTO.CommentCreateRequestDTO req,
                                                             Long userId,
                                                             Long postId) {
        // 지운 글에는 더 달지 않는다. 목록에서 사라진 글에 댓글이 계속 쌓이고 있었다.
        Post post = postRepository.findByIdAndIsDeletedFalse(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
        Comment comment = new Comment(post, userId, null, req.content(), isForcedAnonymous(post));
        Comment saved = commentRepository.save(comment);
        return commentMapper.toCommentCreateResponseDTO(saved);
    }

    /**
     * 글 단위로 댓글 익명이 강제되는지. 건의 게시판은 전부, 자유게시판은 글이 익명일 때.
     * 어느 엔드포인트로 들어오든(일반 댓글·답글·게시판 전용) 같은 규칙을 타야 실명이 새지 않는다.
     */
    boolean isForcedAnonymous(Post post) {
        if (post.getCategory() == PostCategory.SUGGESTION.getValue()) {
            return true;
        }
        return postFreeboardRepository.findByPostId(post.getId())
                .map(fb -> fb.isAnonymous())
                .orElse(false);
    }

    /*
    답글 생성 메소드입니다.
    댓글과 달리 특정 댓글에 연결되기 때문에 postId 대신 commentId를 받아 처리합니다.
    조회한 댓글에 답글을 생성해 parentComment에 addReply로 연결하여 답글 목록에 추가합니다.
    */

    @Transactional
    public CommentDTO.ReplyCreateResponseDTO createReply(CommentDTO.ReplyCreateRequestDTO req,
                                                         Long userId,
                                                         Long commentId) {

        Comment target = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        // 삭제된 댓글에는 답글을 달 수 없다(화면에는 "삭제된 댓글입니다" 자리만 남아 있다)
        if (target.isDeleted()) {
            throw new EntityNotFoundException("Comment not found");
        }
        Comment reply = new Comment(target.getPost(), userId, target, req.content(), isForcedAnonymous(target.getPost()));
        target.addReply(reply);
        Comment saved = commentRepository.save(reply);
        return commentMapper.toReplyCreateResponseDTO(saved);
    }

    /*
    포스트에 달린 댓글을 모두 불러오는 메소드입니다.
    답글은 댓글에 붙어서 오기에, 답글이 아닌댓글(부모댓글이 없는 댓글)만 불러온후, 답글 목록을 포함한 댓글의 DTO로 변환시킨후 반환합니다
     */
    public List<CommentDTO.CommentInfoDTO> getComments(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream().map(comment -> commentMapper.toCommentInfoDTO(comment)).toList();
    }

    /*
    댓글과 답글의 엔티티는 같기에, update에서는 다르게 취급하지 않습니다
     */
    @Transactional
    public void updateComment(Long commentId, CommentDTO.CommentUpdateRequestDTO req,Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        if(!isAuthor(userId,comment)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        comment.changeContent(req.content());
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        boolean isAdminOrManager = user.getRole().isPresidentOrVicePresidentOrAdmin() || user.getRole() == Role.ROLE_MANAGER;
        if (!isAdminOrManager && !isAuthor(userId, comment)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        comment.Delete();
    }

    public List<CommentDTO.CommentAnonymousInfoDTO> getAnonymousComments(Long postId) {
        postFreeboardRepository.findByPostId(postId)
                .filter(fb -> fb.isAnonymous())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "익명 게시글이 아닙니다"));
        return commentRepository.findByPostId(postId).stream()
                .map(commentMapper::toCommentAnonymousInfoDTO)
                .toList();
    }

    @Transactional
    public CommentDTO.CommentCreateResponseDTO createFreeBoardComment(
            CommentDTO.CommentCreateRequestDTO req, Long userId, Long postId, boolean isAnonymous) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        // 자유게시판 경로로 들어와도 건의 글·익명 글이면 실명 저장이 되지 않게 같은 규칙을 탄다
        boolean anonymous = isForcedAnonymous(post) || isAnonymous;
        Comment comment = new Comment(post, userId, null, req.content(), anonymous);
        return commentMapper.toCommentCreateResponseDTO(commentRepository.save(comment));
    }

    public List<CommentDTO.FreeBoardCommentResponse> getFreeBoardComments(Long postId, Long requesterId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return commentRepository.findByPostId(postId).stream()
                .map(c -> commentMapper.toFreeBoardCommentDTO(c, requesterId))
                .toList();
    }

    boolean isAuthor(Long userId,Comment comment){
        if (userId.equals(comment.getUserId())){
            return true;
        }
        return false;
    }
}
