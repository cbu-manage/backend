package com.example.cbumanage.controller;

import com.example.cbumanage.dto.PostDTO;
import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.PostService;
import com.example.cbumanage.utils.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
@Tag(name = "보고서 게식글 관리 컨틀로러")
public class PostReportController {
    private final PostService postService;
    private final JwtProvider jwtProvider;

    @Autowired
    public PostReportController(PostService postService, JwtProvider jwtProvider) {

        this.postService = postService;
        this.jwtProvider = jwtProvider;
    }

    //쿠키에서 userId를 추출하는 코드 입니다
    private Long extractUserIdFromCookie(HttpServletRequest httpServletRequest) {
        String token = null;

        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ACCESS_TOKEN".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN not found");
        }

        Map<String, Object> tokenInfo;
        try {
            tokenInfo = jwtProvider.parseJwt(
                    token,
                    Map.of(
                            "user_id", Long.class,
                            "student_number", Long.class,
                            "role", JSONArray.class,
                            "permissions", JSONArray.class
                    )
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }

        Long user_id = (Long) tokenInfo.get("user_id");

        return user_id;

    }


    @Operation(
            summary = "보고서 게시글 생성",
            description = "한번의 요청에 게시글 생성,게시글-보고서 생성 처리. 테스트 중에는 카테고리를 7로 "
    )
    @PostMapping()
    public ResponseEntity<ResultResponse<PostDTO.PostReportCreateResponseDTO>> createPostReport(@Parameter(description = "현재 게시글에서 테스트 할때는 category를 7로하고 테스트합니다") @RequestBody PostDTO.PostReportCreateRequestDTO req,
                                                                                                HttpServletRequest httpServletRequest){
        Long userId = extractUserIdFromCookie(httpServletRequest);
        PostDTO.PostReportCreateResponseDTO responseDTO = postService.createPostReport(req, userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);

    }

    @Operation(
            summary = "보고서 게시글 미리보기 페이징 조회",
            description = "보고서 게시글 목록을 페이징으로 불러옵니다. post,report,group의 정보를 통합해 가져옵니다.테스트 중에는 카테고리가 7인 게시글만 불러옵니다"
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
    public ResponseEntity<ResultResponse<PostDTO.PostReportViewDTO>>  getPostReportView(@PathVariable Long postId,HttpServletRequest httpServletRequest){
        PostDTO.PostReportViewDTO postReportView;
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try{
             postReportView = postService.getPostReportViewDTO(postId, userId);
        }catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e){
            return ResultResponse.error(ErrorCode.NOT_FOUND);
        }
        return ResultResponse.ok(SuccessCode.SUCCESS,postReportView);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ResultResponse<Void>> updatePostReport(@PathVariable Long postId,
                                                                 @RequestBody PostDTO.PostReportUpdateRequestDTO req,
                                                                 HttpServletRequest httpServletRequest){
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try {
            postService.updatePostReport(req,postId,userId);
        } catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        return ResultResponse.ok(SuccessCode.UPDATED,null);
    }

    @Operation(summary = "보고서 승인 메소드",description = "보고서 승인 메소드 입니다. 추후 권한 기능 추가하여 운영자만 허용 가능하게 수정할 예정입니다")
    @PatchMapping("/{postId}/accept")
    public ResponseEntity<ResultResponse<Void>> acceptPostReport(@PathVariable Long postId,HttpServletRequest httpServletRequest){
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try {
            postService.acceptReport(postId,userId);
        }
        catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        return ResultResponse.ok(SuccessCode.UPDATED,null);
    }






}
