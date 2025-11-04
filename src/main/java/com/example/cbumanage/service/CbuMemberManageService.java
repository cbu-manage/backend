package com.example.cbumanage.service;

import com.example.cbumanage.dto.MemberCreateDTO;
import com.example.cbumanage.dto.MemberUpdateDTO;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.DuesRepository;
import com.example.cbumanage.repository.LogRepository;
import com.example.cbumanage.utils.CbuMemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CbuMemberManageService는 CBU 회원에 관한 CRUD(생성, 조회, 수정, 삭제) 및 관련 기능을 제공하는 서비스 클래스입니다.
 */
@Service
public class CbuMemberManageService {

	// 로깅을 위한 Logger 인스턴스 (클래스 이름 기반)
	private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

	// CBU 회원 데이터 접근을 위한 리포지토리
	private CbuMemberRepository memberRepository;

	// 회비 관련 데이터 접근을 위한 리포지토리 (예: 회비 미납 회원 조회 등)
	private DuesRepository duesRepository;

	// 로그 관련 데이터 접근을 위한 리포지토리 (예: 회원 활동 로그 기록 등)
	private LogRepository logRepository;

	// MemberCreateDTO, MemberUpdateDTO 등 DTO와 엔티티 간 매핑을 담당하는 매퍼
	private CbuMemberMapper cbuMemberMapper;

	/**
	 * 생성자.
	 * 외부에서 주입받은 리포지토리와 매퍼를 이용하여 CbuMemberManageService를 초기화합니다.
	 *
	 * @param memberRepository CBU 회원 데이터 접근 리포지토리
	 * @param duesRepository 회비 관련 리포지토리
	 * @param logRepository 로그 관련 리포지토리
	 * @param cbuMemberMapper DTO와 엔티티 간 매핑을 위한 매퍼
	 */
	@Autowired
	public CbuMemberManageService(CbuMemberRepository memberRepository,
								  DuesRepository duesRepository,
								  LogRepository logRepository,
								  CbuMemberMapper cbuMemberMapper) {
		this.memberRepository = memberRepository;
		this.duesRepository = duesRepository;
		this.logRepository = logRepository;
		this.cbuMemberMapper = cbuMemberMapper;
	}

	/**
	 * getMembers 메서드는 주어진 페이지 번호에 해당하는 CBU 회원 목록을 10건씩 페이징하여 반환합니다.
	 *
	 * @param page 조회할 페이지 번호 (0부터 시작)
	 * @return 해당 페이지의 CbuMember 리스트
	 */
	@Transactional
	public List<CbuMember> getMembers(int page) {
		// PageRequest.of(page, 10): 주어진 페이지 번호에 대해 페이지 크기를 10으로 설정하여 조회
		Page<CbuMember> memberPage = memberRepository.findAll(PageRequest.of(page, 10));
		return memberPage.getContent();
	}

	/**
	 * getMembersWithoutDues 메서드는 회비가 없는 회원(회비 미납 또는 면제 등)을 검색어(term)를 기준으로 조회합니다.
	 *
	 * @param term 검색어 또는 조건
	 * @return 조건에 맞는 CbuMember 리스트
	 */
	@Transactional
	public List<CbuMember> getMembersWithoutDues(final String term) {
		return memberRepository.findAllWithoutDues(term);
	}

	/**
	 * createMember 메서드는 새로운 회원 정보를 생성합니다.
	 * MemberCreateDTO를 받아, 매퍼를 이용해 CbuMember 엔티티로 변환 후 데이터베이스에 저장합니다.
	 *
	 * @param memberCreateDTO 회원 생성에 필요한 정보를 담은 DTO
	 * @return 저장된 CbuMember 엔티티
	 */
	@Transactional
	public CbuMember createMember(final MemberCreateDTO memberCreateDTO) {
		// DTO를 엔티티로 매핑
		CbuMember member = cbuMemberMapper.map(memberCreateDTO);
		// 변환된 회원 엔티티를 데이터베이스에 저장
		memberRepository.save(member);
		return member;
	}

	/**
	 * updateUser 메서드는 기존 회원 정보를 업데이트합니다.
	 * MemberUpdateDTO에 포함된 정보를 기준으로, 기존 회원 엔티티를 수정합니다.
	 *
	 * @param memberUpdateDTO 업데이트할 회원 정보를 담은 DTO
	 * @throws MemberNotExistsException 만약 해당 회원이 존재하지 않으면 예외 발생
	 */
	@Transactional
	public void updateUser(MemberUpdateDTO memberUpdateDTO) {
		// 회원 ID로 기존 회원 엔티티 조회, 없으면 예외 발생
		CbuMember cbuMember = memberRepository.findById(memberUpdateDTO.getCbuMemberId())
				.orElseThrow(MemberNotExistsException::new);
		// 매퍼를 통해 DTO의 변경사항을 기존 엔티티에 적용
		cbuMemberMapper.map(memberUpdateDTO, cbuMember);
	}

	/**
	 * deleteMember 메서드는 주어진 회원 ID에 해당하는 회원 정보를 데이터베이스에서 삭제합니다.
	 *
	 * @param studentNumber 삭제할 회원의 ID
	 */
	@Transactional
	public void deleteMember(final Long studentNumber) {
		// 회원 ID에 해당하는 엔티티 삭제
		memberRepository.deleteByStudentNumber(studentNumber);
	}


}
