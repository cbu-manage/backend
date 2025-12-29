package com.example.cbumanage.authentication.intercepter;

import com.example.cbumanage.authentication.authorization.Permission;
import com.example.cbumanage.authentication.dto.AccessAndRefreshTokenObjectDTO;
import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.authentication.entity.RefreshToken;
import com.example.cbumanage.authentication.service.LoginService;
import com.example.cbumanage.model.enums.Role;
import com.example.cbumanage.utils.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * AuthenticationInterceptor는 HTTP 요청에 포함된 JWT 토큰을 확인하고,
 * 해당 요청이 필요한 인증 및 권한을 만족하는지 검증하는 역할을 합니다.
 *
 * 이 인터셉터는 AccessToken과 RefreshToken을 쿠키 또는 요청 속성에서 확인하며,
 * 토큰이 유효하지 않거나 필요한 권한(permission)이 부족하면 요청을 차단하고 401 Unauthorized 상태를 반환합니다.
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

	// 이 인터셉터가 검사할 대상 권한. (예: MEMBER, ADMIN 등)
	private final Permission permission;

	// 로그인 관련 로직(예: reLogin, 쿠키 생성 등)을 수행하는 서비스
	private final LoginService loginService;

	// JWT 파싱 및 검증을 담당하는 유틸리티 클래스
	private final JwtProvider jwtProvider;

	/**
	 * 생성자.
	 * @param permission 이 인터셉터가 검증할 대상 권한
	 * @param loginService 로그인 및 재로그인 관련 로직을 처리하는 서비스
	 * @param jwtProvider JWT 파싱 및 검증을 위한 유틸리티
	 */
	public AuthenticationInterceptor(Permission permission, LoginService loginService, JwtProvider jwtProvider) {
		this.permission = permission;
		this.loginService = loginService;
		this.jwtProvider = jwtProvider;
	}

	/**
	 * preHandle 메서드는 컨트롤러가 실행되기 전에 호출되며,
	 * 요청에 포함된 JWT 토큰을 확인하고 필요한 권한이 있는지 검증합니다.
	 *
	 * @param request  HTTP 요청 객체
	 * @param response HTTP 응답 객체
	 * @param handler  요청을 처리할 핸들러 (컨트롤러 메서드)
	 * @return 모든 검증이 통과되면 true, 아니면 false를 반환하여 요청 처리를 중단합니다.
	 * @throws Exception 예외 발생 시 처리
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 요청 속성에 저장된 AccessToken 객체를 가져옵니다.
		AccessToken accessToken = ((AccessToken) request.getAttribute("ACCESS_TOKEN"));
		// 요청 속성에 저장된 RefreshToken 객체를 가져옵니다.
		RefreshToken refreshToken = ((RefreshToken) request.getAttribute("REFRESH_TOKEN"));

		// 만약 AccessToken이 아직 파싱되어 있지 않다면, 쿠키에서 ACCESS_TOKEN을 찾아 파싱 시도합니다.
		if (accessToken == null) {
			Cookie cookie = null;
			// 요청에 포함된 모든 쿠키를 가져옵니다.
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				// 각 쿠키를 순회하면서 "ACCESS_TOKEN" 이름의 쿠키를 찾습니다.
				for (Cookie c : cookies) {
					if (c.getName().equals("ACCESS_TOKEN")) {
						cookie = c;
						break;
					}
				}
			}
			// ACCESS_TOKEN 쿠키가 존재하면, 해당 JWT 문자열을 파싱하여 토큰 정보를 얻습니다.
			if (cookie != null) {
				Map<String, Object> tokenInfo = jwtProvider.parseJwt(
						cookie.getValue(),
						// JWT의 클레임 정보에 대해 예상하는 타입을 지정합니다.
						Map.of(
								"user_id", Long.class,
								"student_number", Long.class,
								"role", JSONArray.class,
								"permissions", JSONArray.class
						)
				);
				// 파싱된 정보를 바탕으로 AccessToken 객체를 생성합니다.
				accessToken = new AccessToken(
						((Long) tokenInfo.get("user_id")),
						((Long) tokenInfo.get("student_number")),
						// Role 목록을 JSONArray에서 List<Role>로 변환합니다.
						((JSONArray) tokenInfo.get("role")).toList().stream()
								.map(role -> Role.getValue(role.toString()))
								.toList(),
						// Permission 목록을 JSONArray에서 List<Permission>로 변환합니다.
						((JSONArray) tokenInfo.get("permissions")).toList().stream()
								.map(permission -> Permission.getValue(permission.toString()))
								.toList()
				);
				// 파싱된 AccessToken을 요청 속성에 저장하여 이후 재사용할 수 있게 합니다.
				request.setAttribute("ACCESS_TOKEN", accessToken);
			}
		}

		// 만약 AccessToken이 여전히 존재하지 않는다면, RefreshToken을 사용하여 새 AccessToken을 생성 시도합니다.
		if (accessToken == null) {
			// RefreshToken도 없으면, 쿠키에서 REFRESH_TOKEN을 찾습니다.
			if (refreshToken == null) {
				Cookie cookie = null;
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie c : cookies) {
						if (c.getName().equals("REFRESH_TOKEN")) {
							cookie = c;
							break;
						}
					}
				}
				// REFRESH_TOKEN 쿠키가 존재하면, 로그인 서비스를 통해 재로그인을 시도합니다.
				if (cookie != null) {
					AccessAndRefreshTokenObjectDTO accessAndRefreshTokenObjectDTO = loginService.reLogin(cookie.getValue());
					// 재로그인 시 새로 발급받은 AccessToken과 RefreshToken을 사용합니다.
					accessToken = accessAndRefreshTokenObjectDTO.getAccessToken();
					refreshToken = accessAndRefreshTokenObjectDTO.getRefreshToken();
					// 새 토큰 값으로 생성한 쿠키들을 응답에 추가합니다.
					Cookie[] newCookies = loginService.generateCookie(
							accessAndRefreshTokenObjectDTO.getAccessTokenAsString(),
							accessAndRefreshTokenObjectDTO.getRefreshTokenAsString()
					);
					for (int i = 0; i < newCookies.length; i++) {
						response.addCookie(newCookies[i]);
					}
					// 새로 생성한 토큰을 요청 속성에 저장합니다.
					request.setAttribute("ACCESS_TOKEN", accessToken);
					request.setAttribute("REFRESH_TOKEN", refreshToken);
				}
			}
			// 그래도 AccessToken이 생성되지 않았다면, 인증 실패로 처리하여 401 상태를 반환합니다.
			if (accessToken == null) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return false;
			}
		}
		// 마지막으로, 파싱된 AccessToken이 요청에 필요한 권한(permission)을 포함하고 있는지 확인합니다.
		if (!accessToken.getPermission().contains(this.permission)) {
			// 권한이 부족한 경우 401 Unauthorized 상태를 설정하고 요청 처리를 중단합니다.
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return false;
		}

		// 모든 검증이 통과되면, 요청 처리를 계속 진행합니다.
		return true;
	}
}
