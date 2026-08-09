package com.example.cbumanage.feeinfo.repository;

import com.example.cbumanage.feeinfo.entity.FeeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeeInfoRepository extends JpaRepository<FeeInfo, Long> {

    // 싱글턴 설정값이라 항상 1건 이하만 존재
    Optional<FeeInfo> findFirstByOrderByIdAsc();
}
