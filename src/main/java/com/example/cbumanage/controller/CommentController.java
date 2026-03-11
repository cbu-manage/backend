package com.example.cbumanage.controller;


import com.example.cbumanage.dto.CommentDTO;
import com.example.cbumanage.repository.CommentRepository;
import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.CommentService;
import com.example.cbumanage.utils.JwtProvider;
import com.example.cbumanage.utils.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
public class CommentController {
    private final CommentService commentService;
    private final UserIdExtractor userIdExtractor;

    @Autowired
    public CommentController(CommentService commentService,UserIdExtractor userIdExtractor) {
        this.commentService = commentService;
        this.userIdExtractor = userIdExtractor;
    }


    @Operation(
            summary = "코멘트 작성 요청",
            description = "postId를 통해 댓글을 추가하는 요청입니다"
    )
    @PostMapping("post/{postId}/comment")
    public ResponseEntity<ResultResponse<CommentDTO.CommentCreateResponseDTO>> createComment(@RequestBody CommentDTO.CommentCreateRequestDTO req,
                                                                                             @PathVariable Long postId,
                                                                                             HttpServletRequest httpServletRequest) {
        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        CommentDTO.CommentCreateResponseDTO responseDTO = commentService.createComment(req, userId, postId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);
    }

    @Operation(
            summary = "댓글 목록 반환",
            description = "postId를 통해 댓글 목록을 불러옵니다. 댓글과 답글 모두 불러와집니다, " +
                    "<br> 댓글과 답글은 형식적 구분이 없으며, 답글은 parentCommentId 를 포함하여 반환됩니다"

    )
    @GetMapping("post/{postId}/comment")
    public ResponseEntity<ResultResponse<List<CommentDTO.CommentInfoDTO>>> getComments(@PathVariable Long postId) {
        List<CommentDTO.CommentInfoDTO> commentLists = commentService.getComments(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, commentLists);
    }

//    @Operation(
//            summary = "코딩테스트 문제 댓글 작성",
//            description= "problemId를 통해 코딩테스트 문제에 댓글 추가"
//    )
//    @PostMapping("problems/{problemId}/comment")
//    public ResponseEntity<ResultResponse<CommentDTO.CommentCreateResponseDTO>> createProblemComment(@RequestBody CommentDTO.CommentCreateRequestDTO req,
//                                                                                                    @PathVariable Long problemId,
//                                                                                                     HttpServletRequest httpServletRequest) {
//        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
//        CommentDTO.CommentCreateResponseDTO responseDTO = commentService.createCommentProblem(req, userId, problemId);
//        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);
//    }

//    @Operation(
//            summary = "코딩테스트 문제 댓글 목록 작성",
//            description= "problemId를 통해 문제의 댓글 목록을 불러온다. 댓글-답글 트리가 1계층으로 반환된다."
//    )
//    @GetMapping("problems/{problemId}/comment")
//    public ResponseEntity<ResultResponse<List<CommentDTO.CommentInfoDTO>>> getProblemComments(@PathVariable Long problemId) {
//        List<CommentDTO.CommentInfoDTO> commentLists = commentService.getCommentsProblemId(problemId);
//        return ResultResponse.ok(SuccessCode.SUCCESS, commentLists);
//    }

    /*
    댓글에 답글을 추가하는 함수입니다
    입력되는 commentId가 답글이여도, 해당 답글의 부모댓글과 연결되어서 단일계층의 댓글-답글 구조가 유지됩니다
     */
    @Operation(
            summary = "답글 추가",
            description = "CommentId를 통해 해당 댓글에 답글을 추가합니다."

    )
    @PostMapping("comment/{commentId}/reply")
    public ResponseEntity<ResultResponse<CommentDTO.ReplyCreateResponseDTO>> createReply(@RequestBody CommentDTO.ReplyCreateRequestDTO req,
                                                                                         @Parameter(description = "답글을 추가할 댓글의 ID. 답글을 넣을경우 해당 답글의 부모댓글과 자동으로 연결됩니다") @PathVariable Long commentId,
                                                                                         HttpServletRequest httpServletRequest) {
        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        CommentDTO.ReplyCreateResponseDTO replyCreateResponseDTO = commentService.createReply(req, userId, commentId);
        return ResultResponse.ok(SuccessCode.CREATED, replyCreateResponseDTO);
    }

    @Operation(
            summary = "댓글 변경 ",
            description = " 댓글을 변경합니다. 댓글/답글의 구분없이 작동됩니다"
    )
    @PatchMapping("comment/{commentId}")
    public ResponseEntity<ResultResponse<Void>> updateComment(@RequestBody CommentDTO.CommentUpdateRequestDTO req,
                                                              @PathVariable Long commentId,
                                                              HttpServletRequest httpServletRequest) {
        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        try{
            commentService.updateComment(commentId, req, userId);
            return ResultResponse.ok(SuccessCode.UPDATED, null);
        }
        catch (ResponseStatusException e) {
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
    }

    @Operation(
            summary = "댓글 삭제(softDelete)",
            description = "댓글을 삭제 합니다. 댓글과 답글을 구분하지 않으며, softDelete를 사용하여 데이터를 지우지않고 삭제된 댓글로 표기되게만 합니다"
    )
    @DeleteMapping("comment/{commentId}")
    public ResponseEntity<ResultResponse<Void>> deleteComment(@PathVariable Long commentId, HttpServletRequest httpServletRequest) {
        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        try {
            commentService.deleteComment(commentId,userId);
            return ResultResponse.ok(SuccessCode.DELETED, null);
        }
        catch (ResponseStatusException e) {
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }

    }


}
