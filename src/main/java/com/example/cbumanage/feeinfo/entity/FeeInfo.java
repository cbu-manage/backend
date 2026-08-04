package com.example.cbumanage.feeinfo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "회비·입금 계좌 안내 (동아리 전체에 하나만 존재하는 설정값)")
@Entity
@Table(name = "fee_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_info_id")
    private Long id;

    @Schema(description = "은행명", example = "국민은행")
    @Column(nullable = false, length = 50)
    private String bankName;

    @Schema(description = "계좌번호", example = "123456-78-901234")
    @Column(nullable = false, length = 50)
    private String accountNumber;

    @Schema(description = "예금주", example = "홍길동")
    @Column(nullable = false, length = 30)
    private String accountHolder;

    @Schema(description = "회비 금액(원)", example = "30000")
    @Column(nullable = false)
    private int feeAmount;

    @Schema(description = "감면 금액(원, 휴학·졸업 등)", example = "15000")
    @Column(nullable = false)
    private int discountAmount;

    @Schema(description = "납부 마감일")
    @Column(nullable = false)
    private LocalDate paymentDeadline;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private FeeInfo(String bankName, String accountNumber, String accountHolder,
                     int feeAmount, int discountAmount, LocalDate paymentDeadline) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.feeAmount = feeAmount;
        this.discountAmount = discountAmount;
        this.paymentDeadline = paymentDeadline;
    }

    public static FeeInfo create(String bankName, String accountNumber, String accountHolder,
                                  int feeAmount, int discountAmount, LocalDate paymentDeadline) {
        return new FeeInfo(bankName, accountNumber, accountHolder, feeAmount, discountAmount, paymentDeadline);
    }

    public void update(String bankName, String accountNumber, String accountHolder,
                        int feeAmount, int discountAmount, LocalDate paymentDeadline) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.feeAmount = feeAmount;
        this.discountAmount = discountAmount;
        this.paymentDeadline = paymentDeadline;
    }
}
