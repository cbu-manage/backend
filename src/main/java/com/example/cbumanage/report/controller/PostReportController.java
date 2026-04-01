package com.example.cbumanage.report.controller;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.global.response.ErrorCode;
import com.example.cbumanage.global.response.ResultResponse;
import com.example.cbumanage.global.response.SuccessCode;
import com.example.cbumanage.report.service.PostReportService;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.global.util.JwtProvider;
import com.example.cbumanage.auth.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report")
@Tag(name = "보고서 게시글 관리 컨트롤러")
public class PostReportController {
    private final PostReportService postReportService;
    private final UserIdExtractor userIdExtractor;




    @Operation(
            summary = "보고서 게시글 생성",
            description = "한번의 요청에 게시글 생성,게시글-보고서 생성 처리. 테스트 중에는 카테고리를 7로 합니다" +
                    "<br> PostReportCreateRequestDTO를 통해 생성하며, 반환되는 DTO는 PostReportCreateDTO입니다"
    )
    @PostMapping()
    public ResponseEntity<ResultResponse<PostDTO.PostReportCreateResponseDTO>> createPostReport(@Parameter(description = "현재 게시글에서 테스트 할때는 category를 7로하고 테스트합니다, reportImage는 ImageController에서 생성한 url을 넣습니다" +
                                                                                                            "보고서를 생성하는 DTO의 이름은 PostReportCreateRequestDTO이고, 반환되는 DTO는 PostReportCreateResponseDTO입니다") @RequestBody PostDTO.PostReportCreateRequestDTO req,
                                                                                                HttpServletRequest httpServletRequest
                                                                                                )
                                                                                                {
        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        PostDTO.PostReportCreateResponseDTO responseDTO = postReportService.createPostReport(req, userId);
        return ResultResponse.ok(SuccessCode.CREATED, responseDTO);
    }

    @Operation(
            summary = "보고서 게시글 미리보기 페이징 조회",
            description = "보고서 게시글 목록을 페이징으로 불러옵니다. post,report,group의 정보를 통합해 가져옵니다<br>.카테고리가 7인 게시글만 불러옵니다" +
                    "<br>반환되는 형태는 PostReportPreviewDTO를통해 미리보기 형태로 반환됩니다.게시글의 id,이름,작성자 정보,그룹의 정보 등을 반환합니다"
    )

    @GetMapping
    public ResponseEntity<ResultResponse<Page<PostDTO.PostReportPreviewDTO>>> getPostReportPreviews(@RequestParam int page, @RequestParam int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<PostDTO.PostReportPreviewDTO> postReportPreviewDTOs = postReportService.getPostReportPreviewDTOList(pageable);
        return ResultResponse.ok(SuccessCode.SUCCESS, postReportPreviewDTOs);
    }

    @Operation(
            summary = "그룹별 보고서 게시글 미리보기 페이징 조회",
            description = "groupId 기준으로 해당 그룹의 보고서 게시글 목록을 페이징으로 불러옵니다.<br>" +
                    "반환되는 형태는 PostReportPreviewDTO를 통해 미리보기 형태로 반환됩니다."
    )
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ResultResponse<Page<PostDTO.PostReportPreviewDTO>>> getPostReportPreviewsByGroup(
            @PathVariable Long groupId,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<PostDTO.PostReportPreviewDTO> result = postReportService.getGroupPostReportPreviewDTOList(pageable, groupId);
        return ResultResponse.ok(SuccessCode.SUCCESS, result);
    }

    @Operation(
            summary = "보고서 게시글 단건 조회",
            description = "보고서 게시글 단건 조회 메소드입니다. Post와 Report(+Group)를 한번의 요청에 담아 처리합니다.<br>" +
                    "PostReportViewDTO를 반환합니다. 해당 DTO 안에는 PostInfoDTO,PostReportInfoDTO가 담겨있습니다<br>" +
                    "PostReportDTO 안에는 그룹의 정보를 담는 GroupInfoDTO가 포함되어 있습니다"
    )
    @GetMapping("/{postId}")
    public ResponseEntity<ResultResponse<PostDTO.PostReportViewDTO>>  getPostReportView(@PathVariable Long postId,HttpServletRequest httpServletRequest){
        PostDTO.PostReportViewDTO postReportView;
        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        try{
             postReportView = postReportService.getPostReportViewDTO(postId, userId);
        }catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e){
            return ResultResponse.error(ErrorCode.NOT_FOUND);
        }
        return ResultResponse.ok(SuccessCode.SUCCESS,postReportView);
    }

    @Operation(summary = "보고서 게시글 수장 메소드",description = "보고서 게시글 수정메소드 입니다. 작성자 여부를 확인하고 아닐시 거부합니다 ")
    @PatchMapping("/{postId}")
    public ResponseEntity<ResultResponse<Void>> updatePostReport(@PathVariable Long postId,
                                                                 @RequestBody PostDTO.PostReportUpdateRequestDTO req,
                                                                 HttpServletRequest httpServletRequest){
        Long userId = userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        try {
            postReportService.updatePostReport(req,postId,userId);
        } catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        return ResultResponse.ok(SuccessCode.UPDATED,null);
    }

    @Operation(summary = "보고서 승인 메소드",description = "보고서 승인 메소드 입니다. post의 id를 통해 report를 허용상태로 바꿉니다" +
            "운영진의 권한 여부를 판단해 거부하며, 매개변수 없이 보고서의 현재 상태를 변경시킵니다")
    @PatchMapping("/{postId}/accept")
    public ResponseEntity<ResultResponse<Void>> acceptPostReport(@PathVariable Long postId,HttpServletRequest httpServletRequest){
        Long userId =userIdExtractor.extractUserIdFromCookie(httpServletRequest);
        try {
            postReportService.acceptReport(postId,userId);
        }
        catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e){
            return ResultResponse.error(ErrorCode.NOT_FOUND);
        }
        return ResultResponse.ok(SuccessCode.UPDATED,null);
    }






}
