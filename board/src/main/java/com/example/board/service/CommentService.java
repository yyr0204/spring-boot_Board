package com.example.board.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    
    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }

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
    // 게시글별 댓글 수 조회
    public Map<Long, Long> getCommentCountMap() {
        List<Object[]> results = commentRepository.countCommentsGroupedByBoardId();
        return results.stream()
            .collect(Collectors.toMap(
                r -> (Long) r[0],
                r -> (Long) r[1]
            ));
    }

    // 댓글 삭제
    public void deleteComment(Long commentId) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid comment ID"));

        // 하위 댓글이 있으면 Cascade로 자동 삭제되므로, 부모 댓글만 삭제하면 된다
        commentRepository.delete(comment); // 상위 댓글 및 하위 댓글이 모두 삭제된다
    }
}
