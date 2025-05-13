package com.example.board.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.board.domain.Board;
import com.example.board.domain.Comment;
import com.example.board.domain.User;
import com.example.board.repository.CategoryRepository;
import com.example.board.service.BoardService;
import com.example.board.service.CommentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardViewController {

    private final BoardService boardService;
    @Autowired
    private CategoryRepository categoryRepository;
    private final CommentService commentService;

    @GetMapping
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "user/loginForm";
    }

    // 게시글 목록 조회
    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @RequestParam(value = "searchType", required = false) String searchType,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       Model model, CsrfToken token, HttpSession session) {
        try {
            // 로그인 메시지 처리
            handleLoginMessage(session, model);

            // 페이징 및 검색조건- 세션에 저장
            updateSessionForPagingAndSearch(session, page, searchType, keyword);

            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            // 게시글 목록을 Page 객체로 받아옴
            Page<Board> boardPage = boardService.searchBoards(searchType, keyword, pageable);

            // 댓글 수 카운트 맵 조회
            Map<Long, Long> commentCountMap = commentService.getCommentCountMap();

            model.addAttribute("commentCountMap", commentCountMap);
            model.addAttribute("boards", boardPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", boardPage.getTotalPages());
            model.addAttribute("_csrf", token);
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);

            return "board/list";
        } catch (Exception e) {
            return "error/500";
        }
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         HttpSession session, Model model,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            Optional<Board> optionalBoard = boardService.findById(id);
            if (optionalBoard.isEmpty()) {
                redirectAttributes.addFlashAttribute("alertMessage", "게시글이 존재하지 않습니다.");
                return redirectToCurrentPage(session);
            }

            Board board = optionalBoard.get();
            String loginUsername = userDetails != null ? userDetails.getUsername() : null;

            // 권한 체크
            if (!isAdminOrWriter(loginUsername, board.getWriter())) {
                redirectAttributes.addFlashAttribute("alertMessage", "🚫 권한이 없습니다.");
                return redirectToCurrentPage(session);
            }

            List<Comment> comments = commentService.getCommentsByBoardId(id);
            model.addAttribute("board", board);
            model.addAttribute("loginUsername", loginUsername);
            model.addAttribute("comments", comments);
            model.addAttribute("newComment", new Comment());

            // 현재 페이지 정보 추가
            addCurrentPageInfoToModel(session, model);

            return "board/detail";

        } catch (Exception e) {
            return "error/500";
        }
    }

    // 게시글 삭제
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        try {
            Optional<Board> optionalBoard = boardService.findById(id);

            if (optionalBoard.isEmpty()) {
                redirectAttributes.addFlashAttribute("alertMessage", "게시글이 존재하지 않습니다.");
                return redirectToCurrentPage(session);
            }

            Board board = optionalBoard.get();
            String loginUsername = userDetails.getUsername();

            // 권한 확인
            if (!isAdminOrWriter(loginUsername, board.getWriter())) {
                redirectAttributes.addFlashAttribute("alertMessage", "🚫 권한이 없습니다.");
                return redirectToCurrentPage(session);
            }

            boardService.deleteById(id);
            redirectAttributes.addFlashAttribute("alertMessage", "게시글이 성공적으로 삭제되었습니다.");
            return redirectToCurrentPage(session);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertMessage", "게시글 삭제 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    // 게시글 작성 폼
    @GetMapping("/new")
    public String createForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Board board = new Board();
            String loginUsername = userDetails != null ? userDetails.getUsername() : null;
            board.setWriter(loginUsername);
            model.addAttribute("board", board);
            model.addAttribute("categories", categoryRepository.findAll());
            return "board/form";
        } catch (Exception e) {
            return "error/500";
        }
    }

    // 게시글 저장
    @PostMapping("/save")
    public String save(@ModelAttribute Board board) {
        try {
            boardService.save(board);
            return "redirect:/board/list";
        } catch (Exception e) {
            return "error/500";
        }
    }

    // 댓글 작성
    @PostMapping("/{boardId}/comments")
    public String addComment(@PathVariable Long boardId,
                             @RequestParam(required = false) Long parentCommentId,
                             @ModelAttribute Comment comment,
                             RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session) {
        try {
            String loginUsername = userDetails.getUsername();
            comment.setWriter(loginUsername);
            commentService.saveComment(boardId, comment, parentCommentId);

            return "redirect:/board/" + boardId + "?focus=comment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertMessage", "댓글 작성 중 오류가 발생했습니다.");
            return redirectToCurrentPage(session);
        }
    }

    // 댓글 삭제
    @PostMapping("/{boardId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long boardId,
                                @PathVariable Long commentId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        try {
            String loginUsername = userDetails.getUsername();

            Optional<Comment> optionalComment = commentService.findById(commentId);
            if (optionalComment.isEmpty()) {
                redirectAttributes.addFlashAttribute("alertMessage", "댓글을 찾을 수 없습니다.");
                return redirectToBoardDetail(boardId, session);
            }

            Comment comment = optionalComment.get();
            if (!loginUsername.equals(comment.getWriter())) {
                redirectAttributes.addFlashAttribute("alertMessage", "🚫 권한이 없습니다.");
                return redirectToBoardDetail(boardId, session);
            }

            commentService.deleteComment(commentId);
            redirectAttributes.addFlashAttribute("alertMessage", "댓글이 삭제되었습니다.");

            return redirectToBoardDetail(boardId, session);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertMessage", "댓글 삭제 중 오류가 발생했습니다.");
            return redirectToBoardDetail(boardId, session);
        }
    }

    // admin 이거나 작성자인지 확인하는 메서드
    private boolean isAdminOrWriter(String loginUsername, String writerUsername) {
        return "admin".equals(loginUsername) || loginUsername.equals(writerUsername);
    }

    // 로그인 메시지 처리
    private void handleLoginMessage(HttpSession session, Model model) {
        Object loginMessage = session.getAttribute("loginMessage");
        if (loginMessage != null) {
            model.addAttribute("alertMessage", loginMessage.toString());
            session.removeAttribute("loginMessage");
        }
    }

    // 페이징 및 검색조건을 세션에 저장
    private void updateSessionForPagingAndSearch(HttpSession session, int page, String searchType, String keyword) {
        session.setAttribute("currentPage", page);
        session.setAttribute("searchType", searchType);
        session.setAttribute("keyword", keyword);
    }

    // 현재 페이지 정보 추가
    private void addCurrentPageInfoToModel(HttpSession session, Model model) {
        Integer currentPage = (Integer) session.getAttribute("currentPage");
        String searchType = (String) session.getAttribute("searchType");
        String keyword = (String) session.getAttribute("keyword");

        model.addAttribute("currentPage", currentPage != null ? currentPage : 0);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
    }

    // 기존 페이지로 이동
    private String redirectToCurrentPage(HttpSession session) {
        Integer currentPage = (Integer) session.getAttribute("currentPage");
        String searchType = (String) session.getAttribute("searchType");
        String keyword = (String) session.getAttribute("keyword");

        if (currentPage == null) currentPage = 0;
        if (searchType == null) searchType = "";
        if (keyword == null) keyword = "";

        return "redirect:/board/list?page=" + currentPage + "&searchType=" + searchType + "&keyword=" + keyword;
    }

    // 게시글 상세 페이지로 리다이렉트
    private String redirectToBoardDetail(Long boardId, HttpSession session) {
        Integer currentPage = (Integer) session.getAttribute("currentPage");
        String searchType = (String) session.getAttribute("searchType");
        String keyword = (String) session.getAttribute("keyword");

        return "redirect:/board/" + boardId + "?page=" + currentPage + "&searchType=" + searchType + "&keyword=" + keyword;
    }
}
