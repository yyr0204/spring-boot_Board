package com.example.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.board.domain.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
	// CONCAT 문자열 결합
    @Query("SELECT b FROM Board b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Board> findByTitleIgnoreCase(@Param("title") String title, Pageable pageable);
    
    // 대소문자 구분 없이 내용에서 검색하는 메서드
    @Query("SELECT b FROM Board b WHERE LOWER(b.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    Page<Board> findByContentIgnoreCase(@Param("content") String content, Pageable pageable);
    
    // 카테고리나 제목을 대소문자 구분 없이 검색하는 메서드
    @Query("SELECT b FROM Board b WHERE LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%')) OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Board> findByCategoryOrTitleIgnoreCase(@Param("category") String category, @Param("title") String title, Pageable pageable);
}