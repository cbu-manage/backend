package com.example.cbumanage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDueDTO {
    private String name;
    private Boolean due;
}
