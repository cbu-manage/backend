package com.example.cbumanage.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberMailUpdateDTO {

    @Schema(description = "본인 학번. 생략할 수 있으며, 보내는 경우 로그인한 본인의 학번과 같아야 한다.")
    private Long studentNumber;

    @Schema(description = "새로 등록할 학교 이메일(@tukorea.ac.kr)")
    private String email;

    @Schema(description = "위 이메일로 받은 인증번호. /mail/send 로 발급받은 값이어야 한다.")
    private String authCode;
}
