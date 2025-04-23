package com.example.board.service;

import com.example.board.repository.BoardRepository;
import com.example.board.domain.Board;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // 전체 게시글 조회 (페이징 없이)
//    public List<Board> findAll() {
//        return boardRepository.findAll();
//    }

    // 특정 게시글 조회
    public Optional<Board> findById(Long id) {
        return boardRepository.findById(id);
    }

    // 게시글 저장
    public Board save(Board board) {
        return boardRepository.save(board);
    }

    // 게시글 삭제
    public void deleteById(Long id) {
        boardRepository.deleteById(id);
    }

    // 페이징 처리된 게시글 조회
//    public Page<Board> findPaginated(Pageable pageable) {
//        return boardRepository.findAll(pageable); // Pageable 객체를 그대로 사용
//    }
    
    public Page<Board> searchBoards(String searchType, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return boardRepository.findAll(pageable);
        }

        switch (searchType) {
            case "title":
                return boardRepository.findByTitleIgnoreCase(keyword, pageable);
            case "categoryTitle":
                return boardRepository.findByCategoryOrTitleIgnoreCase(keyword, keyword, pageable);
            case "content":
                return boardRepository.findByContentIgnoreCase(keyword, pageable);
            default:
                return boardRepository.findAll(pageable);
        }
    }

}
