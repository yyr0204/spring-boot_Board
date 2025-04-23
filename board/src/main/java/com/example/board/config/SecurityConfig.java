package com.example.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration  // 이 클래스가 스프링 설정 클래스임을 나타냄
@EnableWebSecurity  // Spring Security를 활성화하는 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {
	
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 요청에 대한 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 로그인, 회원가입 페이지와 정적 리소스(css/js)는 인증 없이 접근 허용
                .requestMatchers("/user/login", "/user/register", "/css/**", "/js/**","/h2-console/**").permitAll()
                // "/board/**" 로 시작하는 경로는 인증된 사용자만 접근 가능
                .requestMatchers("/board/**").authenticated()
                // 그 외 모든 요청은 모두 접근 허용
                .anyRequest().permitAll()
            )
            // CSRF 설정 (H2 Console은 제외)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // [추가]
            )
            // 로그인 관련 설정
            .formLogin(form -> form
                // 사용자 정의 로그인 페이지의 경로 지정
                .loginPage("/user/login")
                // 로그인 요청을 처리할 URL (form의 action 속성)
                .loginProcessingUrl("/login")
                // 로그인 성공 시 사용자 정의 핸들러 호출
                .successHandler(customLoginSuccessHandler)
                // 로그인 실패 시 이동할 URL
                .failureUrl("/user/login?error")
                // 로그인 관련 URL은 모두 접근 허용
                .permitAll()
            )
            // 로그아웃 관련 설정
            .logout(logout -> logout
                // 로그아웃 요청을 처리할 경로 지정
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                // 로그아웃 성공 후 이동할 URL
                .logoutSuccessUrl("/user/login")
                // 세션 무효화
                .invalidateHttpSession(true)
                // JSESSIONID 쿠키 삭제
                .deleteCookies("JSESSIONID")
                // 인증 정보 초기화
                .clearAuthentication(true)
            )
            // HTTP 응답 헤더 관련 설정 (캐시 무효화)
            .headers(headers -> headers
            	.frameOptions(frame -> frame.sameOrigin()) // [추가]
                // 캐시 방지를 위한 헤더 추가
                .addHeaderWriter(new StaticHeadersWriter("Cache-Control", "no-cache, no-store, must-revalidate"))
                .addHeaderWriter(new StaticHeadersWriter("Pragma", "no-cache"))
                .addHeaderWriter(new StaticHeadersWriter("Expires", "0"))
            )
            // 세션 관리 설정
            .sessionManagement(session -> session
                // 세션이 유효하지 않을 때 이동할 URL
            	.invalidSessionUrl("/user/login?timeout=true") // 타임아웃 시 전달
                .maximumSessions(1)                   // 동시에 한 명만 로그인 가능
                .maxSessionsPreventsLogin(false)      // true로 설정 시 중복 로그인 차단, false는 기존 세션 만료 후 로그인 허용
            );

        // 설정을 기반으로 SecurityFilterChain 객체를 생성하여 반환
        return http.build();
    }

    // 비밀번호를 암호화하기 위한 PasswordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 해시 함수를 사용한 비밀번호 암호화기
        return new BCryptPasswordEncoder();
    }
}
