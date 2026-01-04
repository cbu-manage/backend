package com.example.cbumanage.controller;


import com.example.cbumanage.dto.CommentDTO;
import com.example.cbumanage.repository.CommentRepository;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class CommentController {
    private final CommentService commentService;
    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("post/{postId}/comment")
    public ResponseEntity<ResultResponse<CommentDTO.CommentCreateResponseDTO>> createComment(@RequestBody CommentDTO.CommentCreateRequestDTO req,
                                                                                           @PathVariable Long postId,
                                                                                           @RequestParam Long userId){
        CommentDTO.CommentCreateResponseDTO responseDTO = commentService.createComment(req, userId, postId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);
    }

    @GetMapping("post/{postId}/comment")
    public  ResponseEntity<ResultResponse<List<CommentDTO.CommentInfoDTO>>> getComments(@PathVariable Long postId){
        List<CommentDTO.CommentInfoDTO> commentLists =  commentService.getComments(postId);
        return  ResultResponse.ok(SuccessCode.SUCCESS, commentLists);
    }

    /*
    댓글에 답글을 추가하는 함수입니다
    입력되는 commentId가 답글이여도, 해당 답글의 부모댓글과 연결되어서 단일계층의 댓글-답글 구조가 유지됩니다
     */
    @PostMapping("comment/{commentId}/reply")
    public ResponseEntity<ResultResponse<CommentDTO.ReplyCreateResponseDTO>> createReply(@RequestBody CommentDTO.ReplyCreateRequestDTO req,
                                                         @PathVariable Long commentId,
                                                         @RequestParam Long userId){
        CommentDTO.ReplyCreateResponseDTO replyCreateResponseDTO = commentService.createReply(req,userId,commentId);
        return ResultResponse.ok(SuccessCode.CREATED, replyCreateResponseDTO);
    }

    @PatchMapping("comment/{commentId}")
    public ResponseEntity<ResultResponse<Void>> updateComment(@RequestBody CommentDTO.CommentUpdateRequestDTO req,
                              @PathVariable Long commentId){
        commentService.updateComment(commentId, req);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @DeleteMapping("comment/{commentId}")
    public ResponseEntity<ResultResponse<Void>> deleteComment(@PathVariable Long commentId){
        commentService.deleteComment(commentId);
        return ResultResponse.ok(SuccessCode.DELETED, null);
    }


}
