package com.example.cbumanage.feeinfo.dto;

import com.example.cbumanage.feeinfo.entity.FeeInfo;

import java.time.LocalDate;

public record FeeInfoResponse(
        String bankName,
        String accountNumber,
        String accountHolder,
        int feeAmount,
        int discountAmount,
        LocalDate paymentDeadline
) {
    public static FeeInfoResponse from(FeeInfo feeInfo) {
        return new FeeInfoResponse(
                feeInfo.getBankName(),
                feeInfo.getAccountNumber(),
                feeInfo.getAccountHolder(),
                feeInfo.getFeeAmount(),
                feeInfo.getDiscountAmount(),
                feeInfo.getPaymentDeadline()
        );
    }
}
