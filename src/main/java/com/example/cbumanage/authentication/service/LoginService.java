package com.example.cbumanage.authentication.service;

import com.example.cbumanage.authentication.authorization.Permission;
import com.example.cbumanage.authentication.dto.*;
import com.example.cbumanage.authentication.entity.LoginEntity;
import com.example.cbumanage.authentication.entity.RefreshToken;
import com.example.cbumanage.authentication.exceptions.InvalidEmailException;
import com.example.cbumanage.authentication.exceptions.InvalidPasswordException;
import com.example.cbumanage.authentication.exceptions.MemberExistException;
import com.example.cbumanage.authentication.repository.LoginRepository;
import com.example.cbumanage.authentication.repository.RefreshTokenRepository;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.SuccessCandidate;
import com.example.cbumanage.model.enums.Role;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.SuccessCandidateRepository;
import com.example.cbumanage.service.CandidateAppendService;
import com.example.cbumanage.utils.HashUtil;
import com.example.cbumanage.utils.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class LoginService {

	private final EmailManager emailManager;

	private final CbuMemberRepository cbuMemberRepository;
	private final LoginRepository loginRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	@Autowired
	SuccessCandidateRepository successCandidateRepository;

	@Autowired
	CandidateAppendService candidateAppendService;

	private final JwtProvider jwtProvider;
	private final HashUtil hashUtil;
	private final String salt;

	private final long accessTokenExpireTime;
	private final long refreshTokenExpireTime;

	public LoginService(EmailManager emailManager, CbuMemberRepository cbuMemberRepository, LoginRepository loginRepository, RefreshTokenRepository refreshTokenRepository, JwtProvider jwtProvider, HashUtil hashUtil, @Value("${cbu.login.salt}") String salt, @Value("${cbu.jwt.expireTime}") Long accessTokenExpireTime, @Value("${cbu.jwt.refreshExpireTime}") Long refreshTokenExpireTime) {
		this.emailManager = emailManager;
		this.cbuMemberRepository = cbuMemberRepository;
		this.loginRepository = loginRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtProvider = jwtProvider;
		this.hashUtil = hashUtil;
		this.salt = salt == null ? "p394huwtfp9q3a4" : salt;
		this.accessTokenExpireTime = accessTokenExpireTime / 1000;
		this.refreshTokenExpireTime = refreshTokenExpireTime / 1000;
	}

	/**
	 * @throws MemberNotExistsException No member match with 'email'
	 * @throws InvalidPasswordException When 'password' is incorrect
	 */
	@Transactional
	public AccessAndRefreshTokenDTO login(final StudentNumberAndPasswordDTO dto) {
		// 학생 번호로 CbuMember 엔티티 조회, 없으면 예외 발생
		CbuMember cbuMember = cbuMemberRepository.findByStudentNumber(dto.getStudentNumber())
				.orElseThrow(MemberNotExistsException::new);

		// cbuMember의 고유 ID를 이용하여 로그인 엔티티 조회
		LoginEntity login = loginRepository.findById(cbuMember.getCbuMemberId())
				.orElseThrow(MemberNotExistsException::new);

		// 비밀번호 검증: 입력한 비밀번호를 해싱한 값과 DB에 저장된 비밀번호 비교
		if (!login.getPassword().equals(hashUtil.hash(dto.getPassword() + salt))) {
			throw new InvalidPasswordException();
		}

		// 관리자와 일반 회원에 따라 토큰 생성 로직을 분기
		if (dto.getStudentNumber() == 8001202011L) {
			// 관리자 계정 처리 예시
			List<Role> adminRoles = List.of(Role.ADMIN);
			Long exp = jwtProvider.currentTime() + 86400000;
			RefreshToken refreshToken = new RefreshToken(login.getUserId(), exp);
			AccessToken accessToken = new AccessToken(login.getUserId(), login.getStudentNumber(), adminRoles, login.getPermissions());
			refreshTokenRepository.save(refreshToken);
			return generateToken(accessToken, refreshToken);
		} else {
			// 일반 회원 처리: cbuMember의 역할 정보를 사용
			Long exp = jwtProvider.currentTime() + 86400000;
			RefreshToken refreshToken = new RefreshToken(login.getUserId(), exp);
			AccessToken accessToken = new AccessToken(login.getUserId(), login.getStudentNumber(), cbuMember.getRole(), login.getPermissions());
			refreshTokenRepository.save(refreshToken);
			return generateToken(accessToken, refreshToken);
		}
	}


	/**
	 * Generates a new access token and refresh token using existing refresh token.
	 */
	@Transactional
	public AccessAndRefreshTokenObjectDTO reLogin(final String refreshToken) {
		// Jwt validation check
		Map<String, Object> tokenInfo = jwtProvider.parseJwt(refreshToken, Map.of("uuid", UUID.class));

		Long exp = jwtProvider.currentTime() + 86400000;
		RefreshToken refresh = refreshTokenRepository.findById(((UUID) tokenInfo.get("uuid"))).orElseThrow(() -> new NoSuchElementException("There is no refresh token"));
		LoginEntity login = loginRepository.findById(refresh.getUserId()).orElseThrow(MemberNotExistsException::new);
		CbuMember cbuMember = cbuMemberRepository.findById(refresh.getUserId()).orElseThrow(MemberNotExistsException::new);
		AccessToken access = new AccessToken(login.getUserId(), login.getStudentNumber(), cbuMember.getRole(), login.getPermissions());
		refresh.setExp(exp);

		AccessAndRefreshTokenDTO accessAndRefreshTokenDTO = generateToken(access, refresh);
		return new AccessAndRefreshTokenObjectDTO(access, refresh, accessAndRefreshTokenDTO.getAccessToken(), accessAndRefreshTokenDTO.getRefreshToken());
	}

	public AccessAndRefreshTokenDTO generateToken(AccessToken accessToken, RefreshToken refreshToken) {
		return new AccessAndRefreshTokenDTO(
				jwtProvider.generateJwt("JWT", Map.of(
						"user_id", accessToken.getUserId(),
						"student_number", accessToken.getStudentNumber(),
						"role", accessToken.getRole(),
						"permissions", accessToken.getPermission()
				)),
				jwtProvider.generateJwt("JWT", Map.of(
						"user_id", refreshToken.getUserId(),
						"uuid", refreshToken.getId(),
						"exp", refreshToken.getExp()
				))
		);
	}

	/**
	 *
	 * @param accessToken
	 * @param refreshToken
	 * @return Return array of cookies. Index 0 is access token and 1 is refresh token
	 */
	public Cookie[] generateCookie(String accessToken, String refreshToken) {
		Cookie accessTokenCookie = new Cookie("ACCESS_TOKEN", accessToken);
		accessTokenCookie.setSecure(true);
		accessTokenCookie.setMaxAge(((int) accessTokenExpireTime));

		Cookie refreshTokenCookie = new Cookie("REFRESH_TOKEN", refreshToken);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setMaxAge(((int) this.refreshTokenExpireTime));

		return new Cookie[]{accessTokenCookie, refreshTokenCookie};
	}

	/**
	 * @throws MemberExistException When member exist match with email;
	 */
	@Transactional
	public LoginEntity create(SignUpRequestDTO dto) throws IOException {
		// Check email (domain check)
		if (!emailManager.validEmail(dto.getEmail())) throw new InvalidEmailException();

		// Check if email is already in use
		if (loginRepository.findByEmailEquals(dto.getEmail()).isPresent()) throw new MemberExistException("The email is already in use");

		// Check and get 'CbuMember' object
		CbuMember cbuMember = cbuMemberRepository.findByStudentNumber(dto.getStudentNumber()).orElseThrow(() -> new MemberNotExistsException("No member exists with student number"));
		if (!dto.getName().equals(cbuMember.getName())) throw new MemberNotExistsException("No 'CbuMember' object exists with the given name");

		LoginEntity entity = new LoginEntity(cbuMember.getCbuMemberId(), dto.getStudentNumber(), hashUtil.hash(dto.getPassword() + salt), dto.getEmail(), List.of(Permission.MEMBER));
		entity = loginRepository.save(entity);

		SuccessCandidate successCandidate = successCandidateRepository.findByStudentNumber(dto.getStudentNumber());
		candidateAppendService.appendSuccessCandidateToGoogleSheet(successCandidate);

		successCandidateRepository.deleteByStudentNumber(dto.getStudentNumber());

		return entity;
	}

	/**
	 * @throws MemberNotExistsException No member match with 'userId'
	 */
	@Transactional
	public void editPassword(final Long studentNumber, final String password) {
		LoginEntity entity = loginRepository.findBystudentNumber(studentNumber).orElseThrow(MemberNotExistsException::new);
		entity.setPassword(hashUtil.hash(password + this.salt));
	}

	/**
	 * @throws MemberNotExistsException No member match with 'userId'
	 */
	@Transactional
	public void delete(final long userId) {
		LoginEntity entity = loginRepository.findById(userId).orElseThrow(MemberNotExistsException::new);
		List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserId(userId);

		loginRepository.delete(entity);
		refreshTokenRepository.deleteAll(refreshTokens);
	}

	public void clearRefreshToken() {
		List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByExpLessThan(jwtProvider.currentTime());
		refreshTokenRepository.deleteAll(refreshTokens);
	}
}
