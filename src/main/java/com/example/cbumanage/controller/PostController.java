package com.example.cbumanage.controller;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PostDTO.PostReportCreateResponseDTO createPostReport(@RequestBody PostDTO.PostReportCreateRequestDTO req,
                                                          @RequestParam Long userId){
        return postService.createPostReport(req,userId);

    }
    /*
    페이징 기능 구현입니다
    page = 보고싶은 페이지
    size = 페이지 안의 포스트의 개수
    Sort.by(Sort.Order.desc("createdAt")) = 최신 순 정렬
     */

    @GetMapping("post")
    public Page<PostDTO.PostInfoDTO> getPosts(@RequestParam int page,@RequestParam int size,@RequestParam int category){
        Pageable pageable= PageRequest.of(
                page,size, Sort.by(Sort.Order.desc("createdAt"))
        );
        Page<PostDTO.PostInfoDTO> posts=postService.getPostsByCategory(pageable,category);
        return posts;
    }

    @GetMapping("post/{postId}/post")
    public PostDTO.PostInfoDTO getPost(@PathVariable Long postId){
        return postService.getPostById(postId);
    }

    @GetMapping("post/{postId}/report")
    public PostDTO.ReportInfoDTO getPostReport(@PathVariable Long postId){
        return postService.getReportByPostId(postId);
    }

    @PatchMapping("post/report/{postId}")
    public void updatePost(@PathVariable Long postId,@RequestBody PostDTO.PostReportUpdateRequestDTO req){
        postService.updatePostReport(req,postId);
    }

    @DeleteMapping("post/{postId}")
    public void deletePost(@PathVariable Long postId){
        postService.deletePostById(postId);
    }








}
