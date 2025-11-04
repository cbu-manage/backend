package com.example.cbumanage.authentication.scheduler;

import com.example.cbumanage.authentication.service.LoginService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RefreshTokenScheduler는 주기적으로 LoginService의 clearRefreshToken() 메서드를 호출하여,
 * 만료된 리프레시 토큰을 정리(clean up)하는 작업을 수행하는 스케줄러 컴포넌트입니다.
 */
@Component // 이 클래스가 Spring 컨테이너에 빈(bean)으로 등록됨
public class RefreshTokenScheduler {

	// 로그인 관련 비즈니스 로직과 토큰 정리 기능을 제공하는 서비스
	private final LoginService loginService;

	/**
	 * 생성자
	 *
	 * @param loginService 로그인 및 토큰 관리 로직을 수행하는 LoginService 빈을 주입받음
	 */
	public RefreshTokenScheduler(LoginService loginService) {
		this.loginService = loginService;
		// 애플리케이션 시작 시, 초기 리프레시 토큰 정리 작업을 수행합니다.
		loginService.clearRefreshToken();
	}

	/**
	 * @Scheduled 어노테이션에 지정된 cron 표현식에 따라 이 메서드가 실행됩니다.
	 *
	 * cron 표현식 "* * 3 * * *"은 매일 3시(3시 00분 00초부터 3시 59분 59초까지)
	 * 매초마다 이 메서드가 실행됨을 의미합니다.
	 * (즉, 3시 동안 총 3600번 실행됩니다.)
	 *
	 * 만약 의도한 실행 주기가 하루에 단 한 번(예: 3시 정각)이라면,
	 * cron 표현식을 "0 0 3 * * *"과 같이 수정해야 합니다.
	 */
	@Scheduled(cron = "0 0 3 * * *")
	public void run() {
		// LoginService를 통해 만료된 리프레시 토큰들을 정리(clean up)합니다.
		loginService.clearRefreshToken();
	}
}
