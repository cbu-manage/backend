package com.example.cbumanage.controller;

import com.example.cbumanage.service.LogService;
import com.example.cbumanage.service.TukAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@Tag(name = "학교 학생 인증 컨트롤러")
public class TukAuthenticationController {

    TukAuthenticationService tukAuthenticationService;      //로그인 서비스 선언
    LogService logService;          //로그 서비스 선언

    @GetMapping("/api/v1/getLoginKey")
    @Operation(summary = "학교 로그인 키 취득")
    public List<String> getLoginKey(@RequestBody HashMap<String, Object> map){      //학교 로그인 키 전달 함수
        String studentId = (String) map.get("studentId");                           //클라이언트로부터 받아온 아이디
        String studentPw = (String) map.get("studentPw");                           //비밀번호를 학교 api로 요청을 보내 키값을 받아와
        return tukAuthenticationService.getKeys(studentId, studentPw);              //클라이언트에게 전달
    }

}
