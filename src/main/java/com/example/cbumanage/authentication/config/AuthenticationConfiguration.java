package com.example.cbumanage.authentication.config;

import com.example.cbumanage.authentication.authorization.Permission;
import com.example.cbumanage.authentication.intercepter.AuthenticationInterceptor;
import com.example.cbumanage.authentication.repository.RefreshTokenRepository;
import com.example.cbumanage.authentication.service.LoginService;
import com.example.cbumanage.utils.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * AuthenticationConfiguration 클래스는 Spring MVC에서 인증 관련 인터셉터와
 * 메서드 인자 해석기를 설정하기 위한 구성 클래스입니다.
 */
@Configuration
@EnableScheduling  // 스케줄링 기능을 활성화 (예: 주기적인 작업 수행)
public class AuthenticationConfiguration implements WebMvcConfigurer {

	// cbu.jwt.interceptor 프로퍼티를 통해 인터셉터 사용 여부를 결정합니다.
	private final boolean enabled;

	// 컨트롤러 메서드에 인증 정보를 주입하기 위해 사용하는 인자 해석기입니다.
	private final AuthenticationHandlerMethodArgumentResolver authenticationHandlerMethodArgumentResolver;

	// 로그인 관련 서비스로, 토큰 검증 및 사용자 정보를 처리합니다.
	private final LoginService loginService;

	// JWT 관련 기능을 제공하는 유틸리티입니다.
	private final JwtProvider jwtProvider;

	/**
	 * 생성자.
	 * @param enabled cbu.jwt.interceptor 프로퍼티 값 (인터셉터 활성화 여부)
	 * @param refreshTokenRepository RefreshToken 저장소 (여기서는 사용되지 않음, 다른 빈 주입 목적)
	 * @param loginService 로그인 관련 서비스
	 * @param jwtProvider JWT 제공자
	 */
	public AuthenticationConfiguration(
			@Value("${cbu.jwt.interceptor}") boolean enabled,
			RefreshTokenRepository refreshTokenRepository,
			LoginService loginService,
			JwtProvider jwtProvider) {
		this.enabled = enabled;
		// 인자 해석기를 생성하여 컨트롤러 메서드에 인증 정보를 주입할 준비를 합니다.
		authenticationHandlerMethodArgumentResolver = new AuthenticationHandlerMethodArgumentResolver();
		this.loginService = loginService;
		this.jwtProvider = jwtProvider;
	}

	/**
	 * addInterceptors 메서드는 Spring MVC의 인터셉터를 등록하는 역할을 합니다.
	 * 여기서는 Permission 열거형에 정의된 각 권한에 따라 인증 인터셉터를 등록합니다.
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 인터셉터 기능이 비활성화 되어 있으면 추가하지 않습니다.
		if (!enabled) return;

		// Permission 열거형의 모든 상수에 대해 인터셉터를 등록합니다.
		for (Permission p : Permission.values()) {
			// 만약 해당 Permission에 지정된 인증 대상 경로가 없다면 건너뜁니다.
			if (p.getPath().isEmpty()) continue;

			// 새로운 AuthenticationInterceptor를 생성하고 등록합니다.
			// AuthenticationInterceptor는 각 Permission, loginService, jwtProvider를 사용합니다.
			InterceptorRegistration interceptorRegistration = registry.addInterceptor(new AuthenticationInterceptor(p, loginService, jwtProvider));

			// 해당 Permission에 지정된 모든 경로를 인터셉터 적용 대상에 추가합니다.
			p.getPath().forEach(interceptorRegistration::addPathPatterns);

			// 인증을 적용하지 않을 예외 경로를 추가합니다.
			// 여기서는 "/api/v1/validate", "/api/v1/login", "/api/v1/sendMail", "/api/v1/verifyMail" 경로를 인증 제외합니다.
			interceptorRegistration.excludePathPatterns("/api/v1/validate", "/api/v1/login", "/api/v1/sendMail", "/api/v1/verifyMail");

			// Permission에 이미 설정된 추가적인 제외 경로들도 적용합니다.
			p.getExclusivePath().forEach(interceptorRegistration::excludePathPatterns);
		}
	}

	/**
	 * addArgumentResolvers 메서드는 컨트롤러 메서드의 파라미터에 추가적인 해석기를 등록합니다.
	 * 여기서는 AuthenticationHandlerMethodArgumentResolver를 등록하여,
	 * 인증 관련 정보를 컨트롤러 메서드에 주입할 수 있게 합니다.
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(authenticationHandlerMethodArgumentResolver);
	}
}
