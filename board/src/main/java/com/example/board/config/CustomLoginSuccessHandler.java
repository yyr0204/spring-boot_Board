package com.example.board.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
	                                     HttpServletResponse response,
	                                     Authentication authentication) throws IOException, ServletException {

	    String newUser = authentication.getName();  // 새로 로그인한 사용자
	    String previousUser = (String) request.getSession().getAttribute("lastLoggedInUser");

	    // 이전 로그인 사용자와 다른 경우에만 메시지 출력
	    if (previousUser != null && !previousUser.equals(newUser)) {
	        request.getSession().setAttribute("loginMessage", "기존 세션이 끊어지고 새로 로그인되었습니다.");
	    }

	    // 현재 로그인 사용자 정보를 세션에 저장
	    request.getSession().setAttribute("lastLoggedInUser", newUser);

	    response.sendRedirect("/board/list");
	}


}
