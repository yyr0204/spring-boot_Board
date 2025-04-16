package com.example.board.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.board.domain.Board;
import com.example.board.domain.User;
import com.example.board.service.BoardService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardViewController {

    private final BoardService boardService;
    
    @GetMapping
    public String loginForm(Model model) {
    	model.addAttribute("user", new User());
    	return "user/loginForm";
    }
    
    //게시글 목록 조회
    @GetMapping("/list")
    public String list(Model model,CsrfToken token) {
        model.addAttribute("boards", boardService.findAll());
        model.addAttribute("_csrf", token); // csrf 토큰 수동 전달
        return "board/list";
    }

    //게시글 상세 조회
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {

        Optional<Board> optionalBoard = boardService.findById(id);
        if (optionalBoard.isEmpty()) {
        	redirectAttributes.addFlashAttribute("alertMessage", "게시글이 존재하지 않습니다.");
            return "redirect:/board/list";
        }

        Board board = optionalBoard.get();
        String loginUsername = userDetails != null ? userDetails.getUsername() : null;

        // 로그인한 사용자와 게시글 작성자가 다르면 접근 제한
        if (loginUsername != null && !loginUsername.equals(board.getWriter())) {
            redirectAttributes.addFlashAttribute("alertMessage", "🚫 권한이 없습니다.");
            return "redirect:/board/list";
        }

        model.addAttribute("board", board);
        model.addAttribute("loginUsername", loginUsername);

        return "board/detail";
    }



    //게시글 작성 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("board", new Board());
        return "board/form";
    }

    // 게시글 저장
    @PostMapping("/save")
    public String save(@ModelAttribute Board board) {
    	// 현재 로그인한 사용자 ID 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 보통 사용자 ID 또는 이메일

        board.setWriter(username); // 작성자 세팅
        boardService.save(board);
        return "redirect:/board";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {

        Optional<Board> optionalBoard = boardService.findById(id);

        if (optionalBoard.isEmpty()) {
            redirectAttributes.addFlashAttribute("alertMessage", "게시글이 존재하지 않습니다.");
            return "redirect:/board/list"; // 게시글이 존재하지 않으면 목록으로
        }

        Board board = optionalBoard.get();
        String loginUsername = userDetails.getUsername();
        String writerUsername = board.getWriter(); // 게시글 작성자

        if (!loginUsername.equals(writerUsername)) {
            redirectAttributes.addFlashAttribute("alertMessage", "🚫 권한이 없습니다.");
            return "redirect:/board/list"; // 권한이 없으면 목록으로
        }

        boardService.deleteById(id);
        redirectAttributes.addFlashAttribute("alertMessage", "게시글이 성공적으로 삭제되었습니다.");
        return "redirect:/board/list"; // 삭제 후 목록으로 리디렉션
    }






}