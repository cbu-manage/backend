package com.example.cbumanage.member.service;

import com.example.cbumanage.dues.repository.DuesRepository;
import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.member.exception.MemberNotExistsException;
import com.example.cbumanage.member.util.MemberMapper;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

/**
 * 회원 CRUD 및 관련 기능을 제공하는 서비스 클래스입니다.
 */
@Service
public class MemberManageService {

	private final UserRepository userRepository;
	private final DuesRepository duesRepository;
	private final MemberMapper memberMapper;

	@Value("${cbu.login.salt}")
	private String salt;

	@Value("${default.login.password}")
	private String defaultLoginPassword;

	@Autowired
	public MemberManageService(UserRepository userRepository,
								 DuesRepository duesRepository,
								 MemberMapper memberMapper) {
		this.userRepository = userRepository;
		this.duesRepository = duesRepository;
		this.memberMapper = memberMapper;
	}

	@Transactional(readOnly = true)
	public List<User> getMembers(int page) {
		Page<User> memberPage = userRepository.findByDeletedAtIsNull(PageRequest.of(page, 10));
		return memberPage.getContent();
	}

	@Transactional(readOnly = true)
	public List<User> getMembersWithoutDues(final String term) {
		return userRepository.findAllWithoutDues(term);
	}

	@Transactional
	public User createMember(final MemberCreateDTO memberCreateDTO) {
		User member = memberMapper.map(memberCreateDTO, hashPassword(defaultLoginPassword));
		userRepository.save(member);
		return member;
	}

	@Transactional
	public void updateUser(MemberUpdateDTO memberUpdateDTO) {
		User user = userRepository.findByUserIdAndDeletedAtIsNull(memberUpdateDTO.getUserId())
				.orElseThrow(MemberNotExistsException::new);
		memberMapper.map(memberUpdateDTO, user);
	}

	@Transactional
	public void deleteMember(final Long studentNumber) {
		User user = userRepository.findByStudentNumberAndDeletedAtIsNull(studentNumber)
				.orElseThrow(MemberNotExistsException::new);
		user.delete();
	}

	private String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
