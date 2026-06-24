package com.example.cbumanage.email.controller;

import com.example.cbumanage.email.dto.EmailAuthResponseDTO;
import com.example.cbumanage.email.service.EmailService;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.member.dto.MemberMailUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
@Tag(name = "이메일 인증", description = "이메일 인증번호 발송·검증 및 회원 이메일 등록 API입니다.")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "이메일 인증번호 전송", description = "요청한 이메일 주소로 인증번호를 전송합니다.")
    public ApiResponse<EmailAuthResponseDTO> sendAuthCode(@RequestParam String address) {
        return ApiResponse.success(emailService.sendEmail(address));
    }

    @PostMapping("/verify")
    @Operation(summary = "이메일 인증번호 검증", description = "이메일 주소와 인증번호가 일치하는지 검증합니다.")
    public ApiResponse<EmailAuthResponseDTO> checkAuthCode(@RequestParam String address, @RequestParam String authCode) {
        return ApiResponse.success(emailService.validateAuthCode(address, authCode));
    }

    @PostMapping("/update")
    @Operation(summary = "회원 이메일 등록", description = "인증이 완료된 이메일 주소를 회원 정보에 반영합니다.")
    public ApiResponse<Void> updateMail(@RequestBody MemberMailUpdateDTO memberMailUpdateDTO) {
        emailService.updateUserMail(memberMailUpdateDTO);
        return ApiResponse.success();
    }
}
