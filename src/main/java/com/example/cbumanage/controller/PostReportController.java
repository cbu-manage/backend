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
@RequestMapping("/api/v1/report")
@Tag(name = "보고서 게식글 관리 컨틀로러")
public class PostReportController {
    private final PostService postService;
    @Autowired
    public PostReportController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "보고서 게시글 생성",
            description = "한번의 요청에 게시글 생성,게시글-보고서 생성 처리."
    )
    @PostMapping()
    public ResponseEntity<ResultResponse<PostDTO.PostReportCreateResponseDTO>> createPostReport(@Parameter(description = "현재 게시글에서 테스트 할때는 category를 7로하고 테스트합니다") @RequestBody PostDTO.PostReportCreateRequestDTO req,
                                                                                                @RequestParam Long userId){
        PostDTO.PostReportCreateResponseDTO responseDTO = postService.createPostReport(req,userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);

    }

    @Operation(
            summary = "보고서 게시글 미리보기 페이징 조회",
            description = "보고서 게시글 목록을 페이징으로 불러옵니다. post,report,group의 정보를 통합해 가져옵니다"
    )

    @GetMapping
    public ResponseEntity<ResultResponse<Page<PostDTO.PostReportPreviewDTO>>> getPostReportPreviews(@RequestParam int page, @RequestParam int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<PostDTO.PostReportPreviewDTO> postReportPreviewDTOs = postService.getPostReportPreviewDTOList(pageable);
        return ResultResponse.ok(SuccessCode.SUCCESS, postReportPreviewDTOs);
    }

    @Operation(
            summary = "보고서 게시글 단건 조회",
            description = "보고서 게시글 단건 조회 메소드입니다. Post와 Report(+Group)를 한번의 요청에 담아 처리합니다"
    )
    @GetMapping("/{postId}")
    public ResponseEntity<ResultResponse<PostDTO.PostReportViewDTO>>  getPostReportView(@PathVariable Long postId){
        return ResultResponse.ok(SuccessCode.SUCCESS, postService.getPostReportViewDTO(postId));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ResultResponse<Void>> updatePostReport(@PathVariable Long postId,@RequestBody PostDTO.PostReportUpdateRequestDTO req){
        postService.updatePostReport(req,postId);
        return ResultResponse.ok(SuccessCode.UPDATED,null);
    }

    @Operation(summary = "보고서 승인 메소드",description = "보고서 승인 메소드 입니다. 추후 권한 기능 추가하여 운영자만 허용 가능하게 수정할 예정입니다")
    @PatchMapping("/{postId}/accept")
    public ResponseEntity<ResultResponse<Void>> acceptPostReport(@PathVariable Long postId){
        postService.acceptReport(postId);
        return ResultResponse.ok(SuccessCode.UPDATED,null);
    }






}
