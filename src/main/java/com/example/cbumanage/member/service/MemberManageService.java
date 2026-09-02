package com.example.cbumanage.member.service;

import com.example.cbumanage.dues.repository.DuesRepository;
import com.example.cbumanage.application.entity.ApplicationNotification;
import com.example.cbumanage.application.entity.enums.MailNotiType;
import com.example.cbumanage.application.repository.ApplicationNotificationRepository;
import com.example.cbumanage.application.service.RecruitmentService;
import com.example.cbumanage.email.dto.EmailAuthResponseDTO;
import com.example.cbumanage.email.service.EmailService;
import com.example.cbumanage.member.dto.MemberCreateDTO;
import com.example.cbumanage.member.dto.MemberUpdateDTO;
import com.example.cbumanage.member.exception.MemberNotExistsException;
import com.example.cbumanage.member.util.MemberMapper;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 회원 CRUD 및 관련 기능을 제공하는 서비스 클래스입니다.
 */
@Service
public class MemberManageService {

	private static final int MAX_MEMBER_PAGE_SIZE = 100;

	private final UserRepository userRepository;
	private final DuesRepository duesRepository;
	private final MemberMapper memberMapper;
	private final EmailService emailService;
	private final ApplicationNotificationRepository applicationNotificationRepository;
	private final RecruitmentService recruitmentService;

	@Value("${cbu.login.salt}")
	private String salt;

	@Value("${default.login.password}")
	private String defaultLoginPassword;

	@Autowired
	public MemberManageService(UserRepository userRepository,
								 DuesRepository duesRepository,
								 MemberMapper memberMapper,
								 EmailService emailService,
								 ApplicationNotificationRepository applicationNotificationRepository,
								 RecruitmentService recruitmentService) {
		this.userRepository = userRepository;
		this.duesRepository = duesRepository;
		this.memberMapper = memberMapper;
		this.emailService = emailService;
		this.applicationNotificationRepository = applicationNotificationRepository;
		this.recruitmentService = recruitmentService;
	}

	@Transactional(readOnly = true)
	public List<User> getMembers(int page, int size) {
		int pageSize = Math.min(Math.max(size, 1), MAX_MEMBER_PAGE_SIZE);
		Page<User> memberPage = userRepository.findByDeletedAtIsNull(PageRequest.of(page, pageSize));
		return memberPage.getContent();
	}

	@Transactional(readOnly = true)
	public List<User> getMembersWithoutDues(final String term) {
		return userRepository.findAllWithoutDues(term);
	}

	@Transactional
	public User createMember(final MemberCreateDTO memberCreateDTO, final Authentication auth) {
		// 등록 단계에서 역할을 지정하면 그게 곧 역할 부여다. 수정 경로와 같은 기준으로 막는다.
		if (memberCreateDTO.getRole() != null && memberCreateDTO.getRole() != Role.ROLE_USER) {
			assertCanAssignRole(memberCreateDTO.getRole(), auth);
		}
		User member = memberMapper.map(memberCreateDTO, hashPassword(defaultLoginPassword));
		userRepository.save(member);
		// 운영진으로 바로 만들면 진행 중인 모집의 N이 모자란 채로 남아 그 사람 없이도 최종처리가 된다
		if (isVoter(member)) {
			recruitmentService.refreshVoterCount();
		}
		return member;
	}

	@Transactional
	public void updateUser(MemberUpdateDTO memberUpdateDTO, Authentication auth) {
		User user = userRepository.findByUserIdAndDeletedAtIsNull(memberUpdateDTO.getUserId())
				.orElseThrow(MemberNotExistsException::new);
		// 역할이 실제로 바뀔 때만 검사한다. 인원관리가 이름·연락처만 고치며 기존 role을 그대로
		// 재전송하는 정상 요청까지 막으면 안 되므로, 대상의 현재 role을 알 수 있는 여기에 둔다.
		if (memberUpdateDTO.getRole() != null && memberUpdateDTO.getRole() != user.getRole()) {
			assertCanAssignRole(memberUpdateDTO.getRole(), auth);
		}
		memberMapper.map(memberUpdateDTO, user);
		if (memberUpdateDTO.getRole() != null) {
			recruitmentService.refreshVoterCount();
		}
	}

	/**
	 * 역할 부여 권한.
	 *
	 * PATCH/POST /member 는 회원 정보 수정과 역할 지정을 한 엔드포인트에서 처리한다.
	 * 인원관리(ROLE_MEMBER_MANAGER)도 명단 관리를 위해 이 엔드포인트에 접근해야 하는데,
	 * role 필드에 검사가 없어 role 만 실어 보내면 자기 자신을 회장으로 올릴 수 있었다.
	 * 프론트 permissions.ts 의 staff.assign / staff.assignLeader 구분과 같은 기준으로 맞춘다.
	 */
	private void assertCanAssignRole(Role newRole, Authentication auth) {
		Set<String> caller = auth == null
				? Set.of()
				: auth.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.collect(Collectors.toSet());

		boolean leaderOrAdmin = caller.contains(Role.ROLE_ADMIN.name())
				|| caller.contains(Role.ROLE_PRESIDENT.name())
				|| caller.contains(Role.ROLE_VICE_PRESIDENT.name());
		if (!leaderOrAdmin) {
			throw new BaseException(ErrorCode.FORBIDDEN, "역할을 지정할 권한이 없습니다.");
		}

		// 회장·부회장·개발자 계정 임명은 개발자 계정만
		boolean assigningLeader = newRole == Role.ROLE_PRESIDENT
				|| newRole == Role.ROLE_VICE_PRESIDENT
				|| newRole == Role.ROLE_ADMIN;
		if (assigningLeader && !caller.contains(Role.ROLE_ADMIN.name())) {
			throw new BaseException(ErrorCode.FORBIDDEN, "회장·부회장 임명은 개발자 계정만 할 수 있습니다.");
		}
	}

	@Transactional
	public void deleteMember(final Long studentNumber) {
		User user = userRepository.findByStudentNumberAndDeletedAtIsNull(studentNumber)
				.orElseThrow(MemberNotExistsException::new);
		boolean wasVoter = isVoter(user);
		user.delete();
		// 운영진이 빠지면 N이 남아 있어 아무리 투표해도 최종처리가 되지 않는다
		if (wasVoter) {
			recruitmentService.refreshVoterCount();
		}
	}

	private boolean isVoter(User user) {
		return user.getRole() != null && Role.applicationVoterRoles().contains(user.getRole());
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
