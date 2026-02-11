package com.example.cbumanage.controller;

import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/image")
@Tag(name="사진 업로드 컨트롤러")
public class ImageController {
    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(
            summary = "사진업로드",
            description = "사진을 업로드 합니다. 보고서 게시글 작성시 해당 api로 사진을 업로드 한 후," +
                    "보고서 게시글 작성 api의 reportImage에 넣습니다"

    )
    @PostMapping(value = "/upload",consumes = {"multipart/form-data"})
    public ResponseEntity<ResultResponse<String>> upload(@RequestParam("file") MultipartFile file)  throws IOException {
        try {
            return  ResultResponse.ok(SuccessCode.SUCCESS,imageService.uploadImage(file));
        }
        catch (IllegalArgumentException e) {

            String msg = e.getMessage();

            if ("FILE_EMPTY".equals(msg)) {
                return ResultResponse.error(ErrorCode.NOT_FOUND);
            }

            if ("INVALID_FILE_TYPE".equals(msg)) {
                return ResultResponse.error(ErrorCode.NOT_ALLOWED_FILETYPE);

            }

            return ResultResponse.error(ErrorCode.INVALID_REQUEST);

        }
    }
}
