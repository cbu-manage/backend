package com.example.cbumanage.feeinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

@Schema(description = "회비·입금 계좌 안내 등록/수정 요청")
public record FeeInfoRequest(
        @Schema(description = "은행명", example = "국민은행")
        @NotBlank(message = "은행명은 필수입니다.")
        String bankName,

        @Schema(description = "계좌번호", example = "123456-78-901234")
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber,

        @Schema(description = "예금주", example = "홍길동")
        @NotBlank(message = "예금주는 필수입니다.")
        String accountHolder,

        @Schema(description = "회비 금액(원)", example = "30000")
        @PositiveOrZero(message = "회비 금액은 0 이상이어야 합니다.")
        int feeAmount,

        @Schema(description = "감면 금액(원, 휴학·졸업 등)", example = "15000")
        @PositiveOrZero(message = "감면 금액은 0 이상이어야 합니다.")
        int discountAmount,

        @Schema(description = "납부 마감일", example = "2026-09-15")
        @NotNull(message = "납부 마감일은 필수입니다.")
        LocalDate paymentDeadline
) {
}
