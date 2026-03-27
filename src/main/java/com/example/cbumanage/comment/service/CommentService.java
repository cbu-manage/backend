package com.example.cbumanage.comment.service;

import com.example.cbumanage.comment.dto.CommentDTO;
import com.example.cbumanage.comment.entity.Comment;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.problem.entity.Problem;
import com.example.cbumanage.comment.repository.CommentRepository;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.problem.repository.ProblemRepository;
import com.example.cbumanage.comment.util.CommentMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional
    public CommentDTO.CommentCreateResponseDTO createComment(CommentDTO.CommentCreateRequestDTO req,
                                                             Long userId,
                                                             Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found"));
        Comment comment = new Comment(post, userId, null, req.getContent());
        Comment saved = commentRepository.save(comment);
        return commentMapper.toCommentCreateResponseDTO(saved);
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
        Comment reply = new Comment(target.getPost(), userId, target, req.getContent());
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
        comment.changeContent(req.getContent());
    }

    @Transactional
    public void deleteComment(Long commentId,Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        if(!isAuthor(userId,comment)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        comment.Delete();
    }

    boolean isAuthor(Long userId,Comment comment){
        if (userId.equals(comment.getUserId())){
            return true;
        }
        return false;
    }
}
