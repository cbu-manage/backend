package com.example.cbumanage.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberMailUpdateDTO {
    private Long studentNumber;
    private String email;
}
