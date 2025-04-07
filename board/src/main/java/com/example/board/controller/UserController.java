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
    
    //유저 목록 조회
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user/list";
    }
    //로그인 화면
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "user/loginForm";
    }
    //회원가입 화면
    @GetMapping("/register")
    public String userRegister(@ModelAttribute User user) {
    	return "user/registerForm";
    }
    // 회원가입
    @PostMapping("/register")
    public String userRegister(@ModelAttribute User user, Model model) {
        // 회원가입 서비스 호출
        String result = userService.registerUser(user);
        
        if (result.equals("회원가입이 완료되었습니다.")) {
            // 성공 시 로그인 페이지로 리다이렉트
            return "redirect:/user/loginForm";
        } else {
            // 실패 시 에러 메시지와 함께 회원가입 폼을 다시 띄움
            model.addAttribute("error", result);
            return "user/registerForm";  
        }
    }
    
}