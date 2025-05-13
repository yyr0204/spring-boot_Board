package com.example.board.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.board.domain.Board;
import com.example.board.domain.Comment;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // 댓글 또는 대댓글 저장
    public void saveComment(Long boardId, Comment comment, Long parentCommentId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        comment.setBoard(board);
        comment.setCreatedDate(LocalDateTime.now());

        // 대댓글이라면 부모 댓글 설정
        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
            comment.setParentComment(parent);
        }

        commentRepository.save(comment);
    }

    // 특정 게시글의 모든 댓글 조회 (대댓글 포함)
    public List<Comment> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardId(boardId);
    }
}
