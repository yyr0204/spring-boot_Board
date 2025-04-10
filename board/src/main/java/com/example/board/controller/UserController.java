package com.example.board.controller;

import com.example.board.domain.User;
import com.example.board.service.UserService;
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

    // 회원가입 화면
    @GetMapping("/register")
    public String userRegister(@ModelAttribute User user) {
        return "user/registerForm";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String userRegister(@ModelAttribute User user, @RequestParam("confirmPw") String confirmPw, Model model) {
        String result = userService.registerUser(user, confirmPw);
        if ("회원가입이 완료되었습니다.".equals(result)) {
            return "redirect:/user/login"; // 회원가입 성공 시 로그인 페이지로 리다이렉트
        } else {
            model.addAttribute("error", result);
            return "user/registerForm"; // 실패 시 에러 메시지와 함께 폼 유지
        }
    }
}