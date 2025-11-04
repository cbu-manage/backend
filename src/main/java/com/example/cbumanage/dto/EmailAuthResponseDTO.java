package com.example.cbumanage.dto;

import lombok.Getter;

@Getter
public class EmailAuthResponseDTO {
    private boolean success;
    private String responseMessage;

    public EmailAuthResponseDTO(boolean success, String responseMessage){
        this.success = success;
        this.responseMessage = responseMessage;
    }
}