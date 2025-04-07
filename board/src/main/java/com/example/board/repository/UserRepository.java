package com.example.board.repository;

import com.example.board.domain.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // 아이디로 사용자 조회
    Optional<User> findById(String id);
    
    // 아이디 중복 여부 확인
    boolean existsById(String id);

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);
}