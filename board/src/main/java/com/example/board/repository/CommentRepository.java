package com.example.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.board.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 게시글(boardId)에 속한 댓글 전체 조회 (대댓글 포함)
    List<Comment> findByBoardId(Long boardId);

    // 특정 부모 댓글의 대댓글 조회
    List<Comment> findByParentCommentId(Long parentCommentId);
    
    @Query("SELECT c.board.id, COUNT(c) FROM Comment c GROUP BY c.board.id")
    List<Object[]> countCommentsGroupedByBoardId();

}
