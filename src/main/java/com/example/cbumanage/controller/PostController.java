package com.example.cbumanage.controller;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(
            summary = "보고서 게시글 생성",
            description = "한번의 요청에 게시글 생성과, 게시글-보고서 생성을 처리합니다"
    )
    @PostMapping("post/report")
    public ResponseEntity<ResultResponse<PostDTO.PostReportCreateResponseDTO>> createPostReport(@RequestBody PostDTO.PostReportCreateRequestDTO req,
                                                                                              @RequestParam Long userId){
        PostDTO.PostReportCreateResponseDTO responseDTO = postService.createPostReport(req,userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);

    }
    @Operation(
            summary = "프로젝트 게시글 생성",
            description = "프로젝트 관련 정보를 입력받아 새로운 프로젝트 게시글을 생성합니다."
    )
    @PostMapping("post/project")
    public ResponseEntity<ResultResponse<PostDTO.PostProjectCreateResponseDTO>> createPostProject(@RequestBody PostDTO.PostProjectCreateRequestDTO req,
                                                                  @RequestParam Long userId) {
        PostDTO.PostProjectCreateResponseDTO responseDTO = postService.createPostProject(req, userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);
    }


    /*
    페이징 기능 구현입니다
    page = 보고싶은 페이지
    size = 페이지 안의 포스트의 개수
    Sort.by(Sort.Order.desc("createdAt")) = 최신 순 정렬
     */

    @Operation(
            summary = "카테고리 별 포스트 목록 페이징 조회",
            description = "포스트 목록을 페이징으로 불러옵니다. 공통테이블인 Post만 읽습니다"
    )
    @GetMapping("post")
    public ResponseEntity<ResultResponse<Page<PostDTO.PostInfoDTO>>> getPosts(@Parameter(description = "페이지번호") @RequestParam int page,
                                                                              @Parameter(description = "페이지당 post갯수") @RequestParam int size,
                                                                              @Parameter(description = "카테고리") @RequestParam int category){
        Pageable pageable= PageRequest.of(
                page,size, Sort.by(Sort.Order.desc("createdAt"))
        );
        Page<PostDTO.PostInfoDTO> posts=postService.getPostsByCategory(pageable,category);
        return ResultResponse.ok(SuccessCode.SUCCESS, posts);
    }

    @Operation(
            summary = "포스트 메인테이블 단건조회",
            description = "제목, 내용, userId등을 포함한 게시글의 메인테이블의 단건을 조회합니다. 카테고리에 맞는 서브테이블을 불러와야합니다"
    )
    @GetMapping("post/{postId}/post")
    public ResponseEntity<ResultResponse<PostDTO.PostInfoDTO>> getPost(@PathVariable Long postId){
        PostDTO.PostInfoDTO postInfoDTO =  postService.getPostById(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, postInfoDTO);
    }

    @Operation(
            summary = "포스트-보고서 서브테이블 단건조회",
            description = "포스트의 카테고리에 맞는 보고서 서브테이블을 불러옵니다"
    )
    @GetMapping("post/{postId}/report")
    public ResponseEntity<ResultResponse<PostDTO.ReportInfoDTO>> getPostReport(@PathVariable Long postId){
        PostDTO.ReportInfoDTO reportInfoDTO =  postService.getReportByPostId(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, reportInfoDTO);
    }

    @Operation(
            summary = "포스트-프로젝트 서브테이블 단건 조회",
            description = "포스트 ID를 이용해 프로젝트 게시글 상세 정보를 조회합니다."
    )
    @GetMapping("post/{postId}/project")
    public ResponseEntity<ResultResponse<PostDTO.ProjectInfoDTO>> getPostProject(@PathVariable Long postId) {
        PostDTO.ProjectInfoDTO projectInfoDTO =  postService.getProjectByPostId(postId);
        return ResultResponse.ok(SuccessCode.SUCCESS, projectInfoDTO);
    }


    @Operation(
            summary = "보고서 포스트 수정",
            description = "보고서 포스트를 수정합니다. 한번의 요청에 메인테이블과 서브테이블의 수정내용을 담아 처리합니다"
    )
    @PatchMapping("post/report/{postId}")
    public ResponseEntity<ResultResponse<Void>> updatePost(@PathVariable Long postId,@RequestBody PostDTO.PostReportUpdateRequestDTO req){
        postService.updatePostReport(req,postId);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "프로젝트 게시글 수정",
            description = "프로젝트 게시글 ID를 기준으로 메인 테이블인 post와 서브테이블인 프로젝트 정보를 수정합니다."
    )
    @PatchMapping("post/project/{postId}")
    public ResponseEntity<ResultResponse<Void>> updateProject(@PathVariable Long postId, @RequestBody PostDTO.PostProjectUpdateRequestDTO req) {
        postService.updatePostProject(req, postId);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "보고서 포스트 단건 삭제",
            description = "포스트 단건을 softDelete해 포스트 읽어오기에서 필터링 되도록 합니다"
    )
    @DeleteMapping("post/{postId}")
    public ResponseEntity<ResultResponse<Void>> deletePost(@PathVariable Long postId){
        postService.softDeletePost(postId);
        return ResultResponse.ok(SuccessCode.DELETED, null);
    }

//    @Operation(
//            summary = "포스트 단건 삭제",
//            description = "포스트 단건을 softDelete해 포스트 읽어오기에서 필터링 되도록 합니다"
//    )
//    @DeleteMapping("post/{postId}")
//    public ResponseEntity<ResultResponse<Void>> deletePost(@PathVariable Long postId){
//        postService.softDeletePost(postId);
//        return ResultResponse.ok(SuccessCode.DELETED, null);
//    }
}
