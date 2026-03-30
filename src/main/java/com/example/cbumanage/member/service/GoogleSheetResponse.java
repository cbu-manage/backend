package com.example.cbumanage.member.service;

import lombok.Getter;

import java.util.List;

@Getter
public class GoogleSheetResponse {
    private List<List<Object>> values;

    public void setValues(List<List<Object>> values) {
        this.values = values;
    }
}
