package com.example.cbumanage.controller;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "포스트 관리 컨트롤러")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService){
        this.postService = postService;
    }

    @PostMapping("post/report")
    public ResponseEntity<ResultResponse<PostDTO.PostReportCreateResponseDTO>> createPostReport(@RequestBody PostDTO.PostReportCreateRequestDTO req,
                                                                                              @RequestParam Long userId){
        PostDTO.PostReportCreateResponseDTO responseDTO = postService.createPostReport(req,userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);

    }
    /*
    페이징 기능 구현입니다
    page = 보고싶은 페이지
    size = 페이지 안의 포스트의 개수
    Sort.by(Sort.Order.desc("createdAt")) = 최신 순 정렬
     */

    @GetMapping("post")
    public ResponseEntity<ResultResponse<Page<PostDTO.PostInfoDTO>>> getPosts(@RequestParam int page,@RequestParam int size,@RequestParam int category){
        Pageable pageable= PageRequest.of(
                page,size, Sort.by(Sort.Order.desc("createdAt"))
        );
        Page<PostDTO.PostInfoDTO> posts=postService.getPostsByCategory(pageable,category);
        return ResultResponse.ok(SuccessCode.SUCCESS, posts);
    }

    @GetMapping("post/{postId}/post")
    public ResponseEntity<ResultResponse<PostDTO.PostInfoDTO>> getPost(@PathVariable Long postId){
        PostDTO.PostInfoDTO postInfoDTO =  postService.getPostById(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, postInfoDTO);
    }

    @GetMapping("post/{postId}/report")
    public ResponseEntity<ResultResponse<PostDTO.ReportInfoDTO>> getPostReport(@PathVariable Long postId){
        PostDTO.ReportInfoDTO reportInfoDTO =  postService.getReportByPostId(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, reportInfoDTO);
    }

    @PatchMapping("post/report/{postId}")
    public ResponseEntity<ResultResponse<Void>> updatePost(@PathVariable Long postId,@RequestBody PostDTO.PostReportUpdateRequestDTO req){
        postService.updatePostReport(req,postId);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @DeleteMapping("post/{postId}")
    public ResponseEntity<ResultResponse<Void>> deletePost(@PathVariable Long postId){
        postService.deletePostById(postId);
        return ResultResponse.ok(SuccessCode.DELETED, null);
    }








}
