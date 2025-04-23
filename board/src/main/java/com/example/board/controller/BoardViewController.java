package com.example.board.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.board.domain.Board;
import com.example.board.domain.User;
import com.example.board.repository.CategoryRepository;
import com.example.board.service.BoardService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardViewController {

    private final BoardService boardService;
    @Autowired
    private CategoryRepository categoryRepository;
    
    @GetMapping
    public String loginForm(Model model) {
    	model.addAttribute("user", new User());
    	return "user/loginForm";
    }
    
 // 게시글 목록 조회
    @GetMapping("/list")
    public String list(
        @RequestParam(value = "page", defaultValue = "0") int page, // 페이지 번호, 기본값은 0
        @RequestParam(value = "size", defaultValue = "10") int size, // 페이지 당 게시글 개수, 기본값은 10
        @RequestParam(value = "searchType", required = false) String searchType,
        @RequestParam(value = "keyword", required = false) String keyword,
        Model model,
        CsrfToken token,
        HttpSession session) {
        try {
            // 로그인 메시지 처리
            Object loginMessage = session.getAttribute("loginMessage");
            if (loginMessage != null) {
                model.addAttribute("alertMessage", loginMessage.toString());
                session.removeAttribute("loginMessage"); // 메시지 제거해서 새로고침 시 안 뜸
            }
            
            // 현재 페이지 번호를 세션에 저장
            session.setAttribute("currentPage", page);
            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending()); // 페이지 번호, 사이즈, 정렬 설정

            // 게시글 목록을 Page 객체로 받아옴
//            Page<Board> boardPage = boardService.findPaginated(pageable);
            Page<Board> boardPage = boardService.searchBoards(searchType, keyword, pageable);

            model.addAttribute("boards", boardPage.getContent()); // 게시글 목록
            model.addAttribute("currentPage", page); // 현재 페이지
            model.addAttribute("totalPages", boardPage.getTotalPages()); // 전체 페이지 수
            model.addAttribute("_csrf", token); // csrf 토큰 수동 전달 (보안을 위해)
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);

            return "board/list"; // 뷰 이름 반환
        } catch (Exception e) {
            return "redirect:/error/500"; // 예외 발생 시 에러 페이지로 리디렉션
        }
    }


    // 게시글 상세 조회
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         HttpSession session,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            // 게시글 조회
            Optional<Board> optionalBoard = boardService.findById(id);
            if (optionalBoard.isEmpty()) {
                redirectAttributes.addFlashAttribute("alertMessage", "게시글이 존재하지 않습니다.");
                return redirectToCurrentPage(session);
            }

            Board board = optionalBoard.get();

            // 로그인 사용자 이름 가져오기
            String loginUsername = userDetails != null ? userDetails.getUsername() : null;

            // 권한 확인 (작성자 또는 admin만 접근 허용)
            if (loginUsername == null || !isAdminOrWriter(loginUsername, board.getWriter())) {
                redirectAttributes.addFlashAttribute("alertMessage", "🚫 권한이 없습니다.");
                return redirectToCurrentPage(session);
            }

            // 뷰에 데이터 전달
            model.addAttribute("board", board);
            model.addAttribute("loginUsername", loginUsername);

            // 현재 페이지 정보도 모델에 추가 (목록으로 돌아갈 때 사용)
            Integer currentPage = (Integer) session.getAttribute("currentPage");
            model.addAttribute("currentPage", currentPage != null ? currentPage : 0);

            return "board/detail";

        } catch (Exception e) {
            return "redirect:/error/500";
        }
    }

    //게시글 작성 폼
    @GetMapping("/new")
    public String createForm(Model model,  @AuthenticationPrincipal UserDetails userDetails) {
        try {
        	Board board = new Board();
        	// 로그인 사용자 이름 가져오기
            String loginUsername = userDetails != null ? userDetails.getUsername() : null;
            
            board.setWriter(loginUsername);
            model.addAttribute("board",board);
            model.addAttribute("categories", categoryRepository.findAll());
            return "board/form";
        } catch (Exception e) {
            return "redirect:/error/500";
        }
    }

    // 게시글 저장
    @PostMapping("/save")
    public String save(@ModelAttribute Board board) {
    	try {
    		boardService.save(board);
    		return "redirect:/board/list";
		} catch (Exception e) {
			return "redirect:/error/500";
		}
    }
    
    // 게시글 삭제
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) { // 세션을 통해 currentPage 가져오기
        try {
            Optional<Board> optionalBoard = boardService.findById(id);

            if (optionalBoard.isEmpty()) {
                redirectAttributes.addFlashAttribute("alertMessage", "게시글이 존재하지 않습니다.");
                return redirectToCurrentPage(session);
            }

            Board board = optionalBoard.get();
            String loginUsername = userDetails.getUsername();

            if (!isAdminOrWriter(loginUsername, board.getWriter())) {
                redirectAttributes.addFlashAttribute("alertMessage", "🚫 권한이 없습니다.");
                return redirectToCurrentPage(session);
            }

            boardService.deleteById(id);
            redirectAttributes.addFlashAttribute("alertMessage", "게시글이 성공적으로 삭제되었습니다.");
            return redirectToCurrentPage(session);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertMessage", "게시글 삭제 중 오류가 발생했습니다.");
            return "redirect:/error/500";
        }
    }

    
    //  admin 이거나 작성자인지 확인하는 메서드
    private boolean isAdminOrWriter(String loginUsername, String writerUsername) {
        return "admin".equals(loginUsername) || loginUsername.equals(writerUsername);
    }
    // 기존 페이지로 이동
    private String redirectToCurrentPage(HttpSession session) {
        Integer currentPage = (Integer) session.getAttribute("currentPage");
        if (currentPage == null) currentPage = 0;
        return "redirect:/board/list?page=" + currentPage;
    }

}