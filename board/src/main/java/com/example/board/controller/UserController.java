package com.example.board.controller;

import com.example.board.domain.User;
import com.example.board.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    // 로그인 화면
    @GetMapping("/login")
    public String loginForm() {
        return "user/loginForm";
    }
    //로그인
    @PostMapping("/login")
    public String login(@RequestParam String id, @RequestParam String password, HttpSession session, Model model) {
        // 로그인 서비스 호출
        String result = userService.login(id, password);
        if ("로그인 성공".equals(result)) {
            // 로그인 성공 시 세션에 사용자 정보 저장
            User user = userService.findById(id).orElse(null);
            session.setAttribute("user", user);
            return "redirect:/board/list";
        } else {
            // 로그인 실패 시 에러 메시지와 함께 로그인 폼 다시 띄움
            model.addAttribute("error", result);
            return "user/loginForm";
        }
    }
    //로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 무효화
        return "user/loginForm"; 
    }
    //회원가입 화면
    @GetMapping("/register")
    public String userRegister(@ModelAttribute User user) {
    	return "user/registerForm";
    }
    
    //회원가입 처리
    @PostMapping("/register")
    public String userRegister(@ModelAttribute User user, Model model, HttpSession session,@RequestParam("confirmPw") String confirmPw) {
    	System.out.println("회원가입 컨트롤러");
    	
    	if (!user.getPw().equals(confirmPw)) {
            model.addAttribute("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "user/registerForm"; 
        }
    	
        // 회원가입 서비스 호출
        String result = userService.registerUser(user);
        if (result.equals("회원가입이 완료되었습니다.")) {
            // 회원가입 성공 시 로그인 처리
            session.setAttribute("user", user);
            return "redirect:/board/list"; // 게시판 목록 페이지로 리다이렉트
        } else {
            // 실패 시 에러 메시지와 함께 회원가입 폼을 다시 띄움
            model.addAttribute("error", result);
            return "user/registerForm";  
        }
    }
    
}