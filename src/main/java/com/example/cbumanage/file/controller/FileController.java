package com.example.cbumanage.file.controller;

import com.example.cbumanage.file.service.FileService;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/file")
@Tag(name = "파일 업로드 컨트롤러")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "이미지 업로드", description = "jpeg/png/webp/gif/heic/heif 형식만 허용됩니다. S3 URL을 반환합니다.")
    @PostMapping(value = "/image", consumes = {"multipart/form-data"})
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            return ApiResponse.success(fileService.uploadImage(file));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("FILE_EMPTY".equals(msg)) throw new BaseException(ErrorCode.NOT_FOUND);
            if ("INVALID_FILE_TYPE".equals(msg)) throw new BaseException(ErrorCode.NOT_ALLOWED_FILETYPE);
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Operation(summary = "PDF 파일 업로드", description = "PDF 형식만 허용됩니다. 최대 10MB. S3 URL을 반환합니다.")
    @PostMapping(value = "/pdf", consumes = {"multipart/form-data"})
    public ApiResponse<String> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            return ApiResponse.success(fileService.uploadPdf(file));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("FILE_EMPTY".equals(msg)) throw new BaseException(ErrorCode.NOT_FOUND);
            if ("INVALID_FILE_TYPE".equals(msg)) throw new BaseException(ErrorCode.NOT_ALLOWED_FILETYPE);
            if ("FILE_SIZE_EXCEEDED".equals(msg)) throw new BaseException(ErrorCode.FILE_SIZE_EXCEEDED);
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }
    }
}
