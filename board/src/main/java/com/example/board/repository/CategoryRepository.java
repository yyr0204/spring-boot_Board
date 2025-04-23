package com.example.board.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.board.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
