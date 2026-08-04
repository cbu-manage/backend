package com.example.cbumanage.feeinfo.service;

import com.example.cbumanage.feeinfo.dto.FeeInfoRequest;
import com.example.cbumanage.feeinfo.dto.FeeInfoResponse;
import com.example.cbumanage.feeinfo.entity.FeeInfo;
import com.example.cbumanage.feeinfo.repository.FeeInfoRepository;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeeInfoService {

    private final FeeInfoRepository feeInfoRepository;

    @Transactional(readOnly = true)
    public FeeInfoResponse get() {
        FeeInfo feeInfo = feeInfoRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BaseException(ErrorCode.FEE_INFO_NOT_FOUND));
        return FeeInfoResponse.from(feeInfo);
    }

    /**
     * 값이 없으면 새로 생성하고, 있으면 갱신한다 (싱글턴 설정값이라 PUT이 생성까지 겸함).
     */
    @Transactional
    public FeeInfoResponse update(FeeInfoRequest request) {
        FeeInfo feeInfo = feeInfoRepository.findFirstByOrderByIdAsc()
                .map(existing -> {
                    existing.update(request.bankName(), request.accountNumber(), request.accountHolder(),
                            request.feeAmount(), request.discountAmount(), request.paymentDeadline());
                    return existing;
                })
                .orElseGet(() -> feeInfoRepository.save(FeeInfo.create(
                        request.bankName(), request.accountNumber(), request.accountHolder(),
                        request.feeAmount(), request.discountAmount(), request.paymentDeadline())));
        return FeeInfoResponse.from(feeInfo);
    }
}
