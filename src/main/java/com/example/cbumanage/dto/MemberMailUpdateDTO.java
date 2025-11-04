package com.example.cbumanage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberMailUpdateDTO {
    private Long studentNumber;
    private String email;
}
