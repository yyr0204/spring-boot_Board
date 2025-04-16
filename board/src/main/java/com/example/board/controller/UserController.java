package com.example.board.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.domain.User;
import com.example.board.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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

    @PostMapping("/register")
    public String userRegister(@ModelAttribute User user,
                               @RequestParam("confirmPw") String confirmPw,
                               Model model,
                               HttpServletRequest request) {

        String result = userService.registerUser(user, confirmPw);

        if (!"회원가입이 완료되었습니다.".equals(result)) {
            model.addAttribute("error", result);
            return "user/registerForm";
        }
        try {
            UserDetails userDetails = userService.loadUserByUsername(user.getId());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        } catch (Exception e) {
            model.addAttribute("error", "자동 로그인 중 오류가 발생했습니다.");
            return "user/loginForm";
        }
        return "redirect:/board/list";
    }

}