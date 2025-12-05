package com.example.cbumanage.service;

import com.example.cbumanage.dto.CommentDTO;
import com.example.cbumanage.model.Comment;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.repository.CommentRepository;
import com.example.cbumanage.repository.PostRepository;
import com.example.cbumanage.utils.CommentMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;
    @Autowired
    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.postRepository = postRepository;
    }


    @Transactional
    public CommentDTO.CommentCreateResponseDTO createComment(CommentDTO.CommentCreateRequestDTO req,
                                                             Long userId,
                                                             Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(()->new EntityNotFoundException("Post not found"));
        Comment comment=Comment.create(post,userId,null,req.getContent());
        Comment saved=commentRepository.save(comment);
        return commentMapper.toCommentCreateResponseDTO(saved);
    }

    /*
    답글 생성 메소드입니다.
    댓글과 달리 특정 댓글에 연결되기 때문에 postId 대신 commentId를 받아 처리합니다.
    조회한 댓글에 답글을 생성해 parentComment에 addReply로 연결하여 답글 목록에 추가합니다.

    만약 대상 댓글이 이미 답글이라면,
    그 답글의 부모 댓글을 기준으로 답글을 생성해
    부모 댓글 기준으로 통일된 1단계 답글 목록을 유지합니다.
    */

    @Transactional
    public CommentDTO.ReplyCreateResponseDTO createReply(CommentDTO.ReplyCreateRequestDTO req,
                                                         Long userId,
                                                         Long commentId) {

        Comment target = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        Comment parent;
        if (target.getParentComment() == null) {
            parent = target;
        } else {
            parent = target.getParentComment();
        }

        Comment reply = Comment.create(parent.getPost(),userId,parent,req.getContent());
        parent.addReply(reply);
        Comment saved = commentRepository.save(reply);
        return commentMapper.toReplyCreateResponseDTO(saved);
    }

    /*
    포스트에 달린 댓글을 모두 불러오는 메소드입니다.
    답글은 댓글에 붙어서 오기에, 답글이 아닌댓글(부모댓글이 없는 댓글)만 불러온후, 답글 목록을 포함한 댓글의 DTO로 변환시킨후 반환합니다
     */
    public List<CommentDTO.CommentInfoDTO> getComments(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()->new EntityNotFoundException("Post not found"));
        List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);
        return comments.stream().map(comment->commentMapper.toCommentInfoDTO(comment)).toList();
    }

    /*
    댓글과 답글의 엔티티는 같기에, update에서는 다르게 취급하지 않습니다
     */
    @Transactional
    public void updateComment(Long commentId, CommentDTO.CommentUpdateRequestDTO req){
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new EntityNotFoundException("Comment not found"));
        comment.changeContent(req.getContent());
    }
}
