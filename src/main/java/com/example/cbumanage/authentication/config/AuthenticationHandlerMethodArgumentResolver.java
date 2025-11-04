package com.example.cbumanage.authentication.config;

import com.example.cbumanage.authentication.dto.AccessToken; // 컨트롤러에 주입할 인증 토큰 DTO 클래스
import jakarta.servlet.ServletRequest; // ServletRequest를 사용하여 HTTP 요청 정보를 가져옵니다.
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * AuthenticationHandlerMethodArgumentResolver 클래스는 컨트롤러 메서드의 인자 중
 * AccessToken 타입의 인자를 자동으로 주입해주는 역할을 합니다.
 *
 * 이 클래스는 HandlerMethodArgumentResolver 인터페이스를 구현하여,
 * HTTP 요청에 담긴 "ACCESS_TOKEN" 속성값을 컨트롤러 메서드의 AccessToken 파라미터로 전달합니다.
 */
public class AuthenticationHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	/**
	 * supportsParameter 메서드는 현재 리졸버가 특정 메서드 파라미터 타입을 지원하는지 판단합니다.
	 *
	 * @param parameter 현재 처리할 메서드 파라미터 정보
	 * @return 파라미터의 타입이 AccessToken이면 true를 반환, 그렇지 않으면 false를 반환합니다.
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// 파라미터 타입이 AccessToken 클래스와 동일한지 확인합니다.
		return parameter.getParameterType().equals(AccessToken.class);
	}

	/**
	 * resolveArgument 메서드는 실제로 메서드 파라미터에 주입할 값을 결정합니다.
	 *
	 * 여기서는 NativeWebRequest를 통해 기본 ServletRequest를 가져오고,
	 * 그 요청의 "ACCESS_TOKEN" 속성에서 인증 토큰(AccessToken 객체)을 추출하여 반환합니다.
	 *
	 * @param parameter 메서드 파라미터 정보
	 * @param mavContainer 현재 요청의 ModelAndViewContainer (사용되지 않음)
	 * @param webRequest 현재 웹 요청을 나타내는 NativeWebRequest
	 * @param binderFactory 데이터 바인딩에 필요한 WebDataBinderFactory (사용되지 않음)
	 * @return "ACCESS_TOKEN" 속성에 저장된 AccessToken 객체 (없으면 null)
	 * @throws Exception 처리 중 발생할 수 있는 예외
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter,
								  ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest,
								  WebDataBinderFactory binderFactory) throws Exception {
		// NativeWebRequest에서 기본 ServletRequest 객체를 가져옵니다.
		ServletRequest request = (ServletRequest) webRequest.getNativeRequest();
		// "ACCESS_TOKEN" 속성에서 인증 토큰 객체를 추출하여 반환합니다.
		return request.getAttribute("ACCESS_TOKEN");
	}
}
