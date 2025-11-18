package com.example.cbumanage.service;

import com.example.cbumanage.authentication.authorization.Permission;
import com.example.cbumanage.authentication.entity.LoginEntity;
import com.example.cbumanage.authentication.repository.LoginRepository;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.utils.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * LoginEntityInitializer는 기존의 CBU 회원 정보를 기반으로
 * LoginEntity(로그인 계정)를 생성하는 배치 작업용 서비스입니다.
 */
@Service
public class CreateLoginEntity {

    // CBU 회원 정보를 조회하기 위한 리포지토리
    private final CbuMemberRepository cbuMemberRepository;

    // 로그인 계정 정보를 저장하기 위한 리포지토리
    private final LoginRepository loginRepository;

    // 비밀번호 해싱에 사용되는 유틸리티
    private final HashUtil hashUtil;

    // 기본 로그인 비밀번호 (예: "1234")
    @Value("${default.login.password}")
    private String defaultPassword;

    // 비밀번호 해싱 시 사용할 salt 값 (application.properties에 설정되어 있어야 함)
    @Value("${cbu.login.salt}")
    private String salt;

    public CreateLoginEntity(CbuMemberRepository cbuMemberRepository, LoginRepository loginRepository, HashUtil hashUtil) {
        this.cbuMemberRepository = cbuMemberRepository;
        this.loginRepository = loginRepository;
        this.hashUtil = hashUtil;
    }

    /**
     * initializeLoginEntities 메서드는 모든 CBU 회원에 대해 LoginEntity를 생성합니다.
     * 각 회원에 대해 기본 로그인 정보를 설정하며, 이미 생성된 계정은 건너뜁니다.
     */
    @Transactional
    public void initializeLoginEntities() {
        // 모든 CBU 회원 조회 (CbuMember는 MemberDTO와 유사한 구조라고 가정)
        List<CbuMember> members = cbuMemberRepository.findAll();

        for (CbuMember member : members) {
            // 이미 로그인 계정이 존재하면 스킵
            if (loginRepository.existsById(member.getCbuMemberId())) {
                continue;
            }

            // 새로운 LoginEntity 객체 생성
            LoginEntity loginEntity = new LoginEntity();
            // 회원의 고유 ID를 userId로 사용
            loginEntity.setUserId(member.getCbuMemberId());
            // 학생 번호를 그대로 설정 (unique, not null)
            loginEntity.setStudentNumber(member.getStudentNumber());
            // 기본 비밀번호 "1234"를 salt와 결합하여 해싱 처리 후 저장
            loginEntity.setPassword(hashUtil.hash(defaultPassword + salt));
            loginEntity.setEmail(null);
            // 기본 권한 설정: 모든 회원은 기본적으로 MEMBER 권한 부여
            loginEntity.setPermissions(List.of(Permission.MEMBER));

            // 생성한 LoginEntity를 데이터베이스에 저장
            loginRepository.save(loginEntity);
        }
    }
}
