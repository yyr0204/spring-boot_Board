package com.example.board.service;

import com.example.board.domain.User;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    // 모든 사용자 조회
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
        return userRepository.findByEmail(email).isPresent();
    }

    // 회원가입
    public String registerUser(User user, String confirmPw) {
        // 비밀번호 확인
        if (!user.getPw().equals(confirmPw)) {
            return "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
        }
        // 아이디 중복 체크
        if (isIdDuplicated(user.getId())) {
            return "이미 존재하는 사용자 ID입니다.";
        }
        // 이메일 중복 체크
        if (user.getEmail() != null && isEmailDuplicated(user.getEmail())) {
            return "이미 등록된 이메일입니다.";
        }
        // 비밀번호 암호화 및 역할 설정
        user.setPw(passwordEncoder.encode(user.getPw()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return "회원가입이 완료되었습니다.";
    }
}