package com.example.cbumanage.member.service;

import com.example.cbumanage.dues.repository.DuesRepository;
import com.example.cbumanage.application.entity.ApplicationNotification;
import com.example.cbumanage.application.entity.enums.MailNotiType;
import com.example.cbumanage.application.repository.ApplicationNotificationRepository;
import com.example.cbumanage.email.dto.EmailAuthResponseDTO;
import com.example.cbumanage.email.service.EmailService;
import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.member.exception.MemberNotExistsException;
import com.example.cbumanage.member.util.MemberMapper;
import com.example.cbumanage.user.entity.MemberStatus;
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
	private final EmailService emailService;
	private final ApplicationNotificationRepository applicationNotificationRepository;

	@Value("${cbu.login.salt}")
	private String salt;

	@Value("${default.login.password}")
	private String defaultLoginPassword;

	@Autowired
	public MemberManageService(UserRepository userRepository,
								 DuesRepository duesRepository,
								 MemberMapper memberMapper,
								 EmailService emailService,
								 ApplicationNotificationRepository applicationNotificationRepository) {
		this.userRepository = userRepository;
		this.duesRepository = duesRepository;
		this.memberMapper = memberMapper;
		this.emailService = emailService;
		this.applicationNotificationRepository = applicationNotificationRepository;
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

	@Transactional
	public void approvePayment(Long userId, boolean newMember) {
		User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
				.orElseThrow(MemberNotExistsException::new);
		user.changeMemberStatus(MemberStatus.ACTIVE);

		// 신규 부원이 아니면 온보딩 메일 발송을 하지 않음
		if (!newMember) {
			return;
		}

		if (user.getEmail() == null || user.getEmail().isBlank()) {
			return;
		}
		EmailAuthResponseDTO result = emailService.sendOnboardingEmail(user.getEmail(), user.getName());
		if (result == null) {
			result = new EmailAuthResponseDTO(false, "메일 발송 결과를 확인할 수 없습니다.");
		}
		if (user.getApplicationId() == null) {
			return;
		}
		ApplicationNotification notification = result.isSuccess()
				? ApplicationNotification.sent(user.getApplicationId(), user.getEmail(), MailNotiType.ONBOARDING)
				: ApplicationNotification.failed(user.getApplicationId(), user.getEmail(), MailNotiType.ONBOARDING,
				result.getResponseMessage());
		applicationNotificationRepository.save(notification);
	}

	@Transactional
	public int deactivateAllActiveMembers() {
		return userRepository.bulkUpdateMemberStatus(MemberStatus.ACTIVE, MemberStatus.INACTIVE);
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
