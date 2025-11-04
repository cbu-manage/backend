package com.example.cbumanage.controller;

import com.example.cbumanage.dto.EmailAuthResponseDTO;
import com.example.cbumanage.dto.MemberMailUpdateDTO;
import com.example.cbumanage.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
@Tag(name = "이메일 인증 컨트롤러", description = "이메일 인증을 위한 컨트롤러입니다.")
public class EmailController {

    private final EmailService emailService;

    // 인증번호 전송
    @PostMapping("/send")
    @Operation(summary = "메일 인증번호 전송", description = "전달받은 메일주소로 인증 메일을 전송합니다.")
    public EmailAuthResponseDTO sendAuthCode(@RequestParam String address) {
        return emailService.sendEmail(address);
    }

    // 인증번호 검증
    @PostMapping("/verify")
    @Operation(summary = "메일 인증번호 검증", description = "인증번호를 검증합니다.")
    public EmailAuthResponseDTO checkAuthCode(@RequestParam String address, @RequestParam String authCode) {
        return emailService.validateAuthCode(address, authCode);
    }

    @PostMapping("/update")
    @Operation(summary = "사용자 메일 등록", description = "인증이 완료된 메일을 회원 정보에 업데이트 합니다.")
    public String updateMail(@RequestBody MemberMailUpdateDTO memberMailUpdateDTO) throws IOException {
        emailService.updateUserMail(memberMailUpdateDTO);



        return "메일 주소 반영 성공!";
    }

}
