package com.example.cbumanage.authentication.controller;

import com.example.cbumanage.authentication.dto.AccessAndRefreshTokenDTO;
import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.authentication.dto.SignUpRequestDTO;
import com.example.cbumanage.authentication.dto.StudentNumberAndPasswordDTO;
import com.example.cbumanage.authentication.exceptions.AuthenticationException;
import com.example.cbumanage.authentication.exceptions.InvalidJwtException;
import com.example.cbumanage.authentication.intercepter.AuthenticationInterceptor;
import com.example.cbumanage.authentication.repository.LoginRepository;
import com.example.cbumanage.authentication.service.LoginService;
import com.example.cbumanage.dto.MemberCreateDTO;
import com.example.cbumanage.model.SuccessCandidate;
import com.example.cbumanage.model.enums.Role;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.SuccessCandidateRepository;
import com.example.cbumanage.service.CbuMemberManageService;
import com.example.cbumanage.utils.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LoginController는 로그인, 회원가입, 비밀번호 변경, 회원 탈퇴와 같은 사용자 인증 관련 엔드포인트를 제공하는 REST 컨트롤러입니다.
 */
@RestController
@Validated
@RequestMapping("/api/v1/login")
@Tag(name = "로그인 컨트롤러", description = "")
public class LoginController {

	// 로그인, 회원가입, 비밀번호 변경 등 인증 관련 기능을 제공하는 서비스
	private final LoginService loginService;

	// 인증 인터셉터를 직접 호출하여 토큰 검증 등을 수행하기 위해 사용합니다.
	private final AuthenticationInterceptor authenticationInterceptor;

	// 회원가입 시 합격자 정보를 조회하기 위한 SuccessCandidateRepository (합격자 데이터베이스)
	@Autowired
	SuccessCandidateRepository successCandidateRepository;

	// 회원가입 시 CBU 회원 관리 관련 서비스를 제공하는 서비스
	@Autowired
	CbuMemberManageService cbuMemberManageService;

	// 회원 데이터 관련 CRUD 처리를 위한 CBU 회원 Repository
	@Autowired
	CbuMemberRepository cbuMemberRepository;

	@Autowired
	LoginRepository loginRepository;

	/**
	 * 생성자. LoginService와 JwtProvider를 주입받아서 인증 인터셉터를 초기화합니다.
	 * @param loginService 로그인 및 회원 관련 로직을 담당하는 서비스
	 * @param jwtProvider JWT 토큰 관련 기능을 제공하는 유틸리티
	 */
	public LoginController(LoginService loginService, JwtProvider jwtProvider) {
		this.loginService = loginService;
		// AuthenticationInterceptor를 생성할 때, 현재 Permission 정보는 필요하지 않으므로 null로 전달합니다.
		authenticationInterceptor = new AuthenticationInterceptor(null, loginService, jwtProvider);
	}

	/**
	 * GET /api/v1/login
	 * 로그인 후, Access와 Refresh 토큰을 쿠키에 설정하여 반환합니다.
	 * 헤더에 이메일과 비밀번호를 포함하여 요청합니다.
	 *
	 * @return
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "로그인 후 쿠키에 토큰 반환", description = "헤더에 학번과 비밀번호를 넣어 요청")
	public Map<String, String> login(@RequestBody StudentNumberAndPasswordDTO studentNumberAndPasswordDTO,HttpServletResponse res) {
		// LoginService의 login 메서드를 호출하여 Access 및 Refresh 토큰을 생성합니다.
		AccessAndRefreshTokenDTO login = loginService.login(new StudentNumberAndPasswordDTO(studentNumberAndPasswordDTO.getStudentNumber(), studentNumberAndPasswordDTO.getPassword()));

		// 토큰 문자열을 포함하는 쿠키 배열을 생성합니다.
		Cookie[] cookies = loginService.generateCookie(login.getAccessToken(), login.getRefreshToken());
		// 각 쿠키를 응답에 추가합니다.
		for (Cookie cookie : cookies) {
			res.addCookie(cookie);
		}
		String cbuMember = cbuMemberRepository.findNameByStudentNumber(studentNumberAndPasswordDTO.getStudentNumber());
		String memberEmail = String.valueOf(loginRepository.findEmailBystudentNumber(studentNumberAndPasswordDTO.getStudentNumber()));

		Map<String, String> response = new HashMap<>();
		response.put("name", cbuMember);
		response.put("email", memberEmail);

		return response;
	}

	/**
	 * POST /api/v1/login
	 * 회원가입 엔드포인트입니다.
	 * JSON 형식의 SignUpRequestDTO를 받아 회원가입을 진행하고, 성공 메시지를 반환합니다.
	 *
	 * @param dto 회원가입에 필요한 정보 (이메일, 비밀번호, 이름, 학번, 닉네임 등)
	 * @return "회원가입 성공!" 문자열
	 * @throws IOException 예외 발생 시 처리
	 */
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "회원가입", description = "json 형식으로 email, password, name, studentNumber, nickname을 넣어 요청")
	public String register(@RequestBody @Valid SignUpRequestDTO dto) throws IOException {
		// 학생 번호로 합격자 데이터를 조회합니다.
		SuccessCandidate successCandidate = successCandidateRepository.findByStudentNumber(dto.getStudentNumber());

		// 새 회원 생성을 위한 DTO 객체 생성 및 필드 설정
		MemberCreateDTO cbuMember = new MemberCreateDTO();
		List<Role> defaultRoles = List.of(Role.MEMBER);  // 기본 역할을 MEMBER로 설정합니다.
		cbuMember.setName(successCandidate.getName());
		cbuMember.setStudentNumber(successCandidate.getStudentNumber());
		cbuMember.setRole(defaultRoles);
		cbuMember.setPhoneNumber(successCandidate.getPhoneNumber());
		cbuMember.setMajor(successCandidate.getMajor());
		cbuMember.setGeneration(23L);
		cbuMember.setNote("");
		cbuMember.setGrade(successCandidate.getGrade());

		// 만약 해당 학번으로 등록된 회원이 없으면, 새로운 회원으로 생성합니다.
		if(cbuMemberRepository.findByStudentNumber(successCandidate.getStudentNumber()).isEmpty()){
			cbuMemberManageService.createMember(cbuMember);
		}

		// 회원가입 후 로그인 서비스의 create 메서드를 호출하여 추가 처리를 진행합니다.
		loginService.create(dto);
		return "회원가입 성공!";
	}

	/**
	 * PATCH /api/v1/login/password
	 * 비밀번호 변경 엔드포인트입니다.
	 * JSON 형식의 UserIdAndPasswordDTO를 받아 비밀번호를 변경합니다.
	 *
	 * @param dto 비밀번호 변경에 필요한 사용자 ID와 새 비밀번호
	 */
	@PatchMapping("/password")
	@Operation(summary = "비밀번호 변경")
	@ResponseStatus(HttpStatus.OK)
	public void editPassword(@RequestBody @Valid StudentNumberAndPasswordDTO dto) {
		loginService.editPassword(dto.getStudentNumber(), dto.getPassword());
	}

	/**
	 * DELETE /api/v1/login
	 * 로그인 엔티티 삭제(회원 탈퇴) 엔드포인트입니다.
	 * 헤더에 USER_ID를 포함하여 요청하면 해당 사용자의 로그인 데이터를 삭제합니다.
	 *
	 * @param userId 삭제할 사용자 ID (헤더에서 전달)
	 * @param request HttpServletRequest를 통해 현재 요청에 담긴 인증 토큰 등을 확인합니다.
	 * @param response HttpServletResponse를 사용하여 응답을 보냅니다.
	 */
	@DeleteMapping
	@Operation(summary = "로그인 엔티티 삭제")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@RequestHeader("USER_ID") Long userId, HttpServletRequest request, HttpServletResponse response) {
		// 토큰을 검증하여 요청자의 권한이 올바른지 확인합니다.
		checkToken(request, response, userId);
		// 토큰 검증이 완료되면 로그인 서비스의 delete 메서드를 호출하여 해당 사용자의 로그인 데이터를 삭제합니다.
		loginService.delete(userId);
	}

	/**
	 * checkToken 메서드는 요청 헤더 및 속성에 저장된 AccessToken을 확인하여,
	 * 요청자가 실제로 해당 userId의 소유자인지 검증합니다.
	 *
	 * @param request HttpServletRequest에서 "ACCESS_TOKEN" 속성을 읽습니다.
	 * @param response HttpServletResponse를 통해 응답 설정을 조정할 수 있습니다.
	 * @param userId 요청에서 전달된 사용자 ID
	 */
	private void checkToken(final HttpServletRequest request, final HttpServletResponse response, final Long userId) {
		try {
			// 인증 인터셉터를 수동으로 호출하여, 요청의 인증 정보를 검증합니다.
			boolean isValid = authenticationInterceptor.preHandle(request, response, null);
			if (!isValid) {
				throw new InvalidJwtException("Token validation failed");
			}
		} catch (InvalidJwtException e) {
			// JWT 관련 예외는 그대로 재던지기
			throw e;
		} catch (Exception e) {
			// 기타 예외는 AuthenticationException으로 래핑
			throw new AuthenticationException("Authentication failed: " + e.getMessage());
		}
		// 요청 속성에 저장된 AccessToken을 가져옵니다.
		AccessToken accessToken = ((AccessToken) request.getAttribute("ACCESS_TOKEN"));
		// AccessToken이 없거나, 토큰에 저장된 사용자 ID와 요청 헤더의 userId가 일치하지 않으면 JWT가 유효하지 않다고 판단합니다.
		if (accessToken == null || !accessToken.getUserId().equals(userId)) {
			throw new InvalidJwtException("Token user ID mismatch");
		}
	}
}