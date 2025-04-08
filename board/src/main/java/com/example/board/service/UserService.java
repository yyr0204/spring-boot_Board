package com.example.board.service;

import com.example.board.repository.UserRepository;
import com.example.board.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Lombok이 자동으로 생성자 추가
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 아이디로 사용자 조회
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    // 이메일로 사용자 조회
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 아이디 중복 체크
    public boolean isIdDuplicated(String id) {
        return userRepository.existsById(id);
    }

    // 이메일 중복 체크
    public boolean isEmailDuplicated(String email) {
        Optional<User> user = findByEmail(email);
        return user.isPresent();
    }

    // 회원가입
    public String registerUser(User user) {
        // 아이디 중복 체크
        if (isIdDuplicated(user.getId())) {
            return "아이디가 이미 존재합니다.";
        }

        // 이메일 중복 체크
        if (isEmailDuplicated(user.getEmail())) {
            return "이메일이 이미 존재합니다.";
        }

        // 회원가입
        userRepository.save(user);
        return "회원가입이 완료되었습니다.";
    }
    // 로그인
    public String login(String id, String password) {
        // 아이디로 사용자 찾기
        Optional<User> userOptional = findById(id);
        if (userOptional.isEmpty()) {
            return "아이디가 존재하지 않습니다."; // 아이디가 없는 경우
        }
        User user = userOptional.get();
        // 비밀번호 체크
        if (!user.getPw().equals(password)) {
            return "비밀번호가 일치하지 않습니다."; // 비밀번호 불일치
        }
        return "로그인 성공"; // 로그인 성공
    }

}

