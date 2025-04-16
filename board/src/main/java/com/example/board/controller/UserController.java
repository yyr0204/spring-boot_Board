package com.example.board.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.board.domain.User;
import com.example.board.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user") // "/user" 경로 하위 요청을 처리하는 컨트롤러
@RequiredArgsConstructor // 생성자 주입을 위한 Lombok 어노테이션
public class UserController {

    private final UserService userService;

    // 로그인 화면 요청 처리
    @GetMapping("/login")
    public String loginForm(HttpServletRequest request) {
    	// 로그인 폼 열 때 기존 사용자 있으면 세션에 저장
        String currentUser = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        if (currentUser != null) {
            request.getSession().setAttribute("previousUser", currentUser);
        }
        return "user/loginForm"; // 로그인 폼 뷰 반환
    }

    // 회원가입 폼 요청 처리
    @GetMapping("/register")
    public String userRegister(@ModelAttribute User user) {
        return "user/registerForm"; // 회원가입 폼 뷰 반환
    }

    // 회원가입 요청 처리 (폼 제출)
    @PostMapping("/register")
    public String userRegister(@ModelAttribute User user,
                               @RequestParam("confirmPw") String confirmPw,
                               Model model,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        // 회원가입 처리 및 결과 메시지 반환
        String result = userService.registerUser(user, confirmPw);

        // 실패 시 메시지 전달 후 다시 회원가입 폼으로 이동
        if (!"회원가입이 완료되었습니다.".equals(result)) {
            model.addAttribute("error", result);
            return "user/registerForm";
        }

        // 기존 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();  // 기존 세션을 무효화
        }

        // 자동 로그인 처리
        try {
            // 방금 등록된 사용자 정보를 다시 로드 (UserDetails 타입으로)
            UserDetails userDetails = userService.loadUserByUsername(user.getId());

            // 인증 토큰 생성 (비밀번호 없이 인증된 객체로 생성)
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 요청 정보를 바탕으로 인증 세부 설정 추가
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Spring Security의 인증 컨텍스트에 저장하여 로그인 처리
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // (선택) 세션에도 인증 정보 저장 (예: 세션 타임아웃 관련 설정 시 활용)
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        } catch (Exception e) {
            // 자동 로그인 실패 시 에러 메시지 출력 후 로그인 폼으로 이동
            model.addAttribute("error", "자동 로그인 중 오류가 발생했습니다.");
            return "user/loginForm";
        }

        // 로그인된 상태로 게시판 리스트 페이지로 리다이렉트
        redirectAttributes.addFlashAttribute("sessionExpired", "기존 세션이 만료되어 새로 로그인되었습니다.");
        return "redirect:/board/list";
    }


}
