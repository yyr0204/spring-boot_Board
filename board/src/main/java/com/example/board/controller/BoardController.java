package com.example.board.controller;

import com.example.board.domain.Board;
import com.example.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 전체 게시글 조회
    @GetMapping
    public List<Board> getAllBoards() {
        return boardService.findAll();
    }

    //게시글 조회
    @GetMapping("/{id}")
    public Optional<Board> getBoardById(@PathVariable Long id) {
        return boardService.findById(id);
    }

    //게시글 등록
    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardService.save(board);
    }

    //게시글 삭제
    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id) {
        boardService.deleteById(id);
    }
}