package com.example.cbumanage.controller;

import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.service.SuccessCandidateSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "합격자 관리 컨트롤러", description = "합격자 명단을 다루는 컨트롤러입니다.")
public class SuccessCandidateController {

    @Autowired
    SuccessCandidateSyncService successCandidateSyncService;

    @PostMapping("candidate/sync")
    @Operation(summary = "스프레드시트 -> 데이터베이스 데이터 연동", description = "스프레드시트의 데이터를 데이터베이스에 주입합니다.")
    public String candidateSync(AccessToken accessToken) {
        successCandidateSyncService.syncSuccessCandidatesFromGoogleSheet();      //스프레드시트에서 데이터베이스로 데이터 값 주입
        return "멤버 저장 성공!";
    }
}
