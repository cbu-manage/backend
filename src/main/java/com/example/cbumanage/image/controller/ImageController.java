package com.example.cbumanage.image.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/image")
@Tag(name = "사진 업로드 컨트롤러")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @Operation(summary = "사진업로드")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            return ApiResponse.success(imageService.uploadImage(file));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("FILE_EMPTY".equals(msg)) throw new BaseException(ErrorCode.NOT_FOUND);
            if ("INVALID_FILE_TYPE".equals(msg)) throw new BaseException(ErrorCode.NOT_ALLOWED_FILETYPE);
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }
    }
}
