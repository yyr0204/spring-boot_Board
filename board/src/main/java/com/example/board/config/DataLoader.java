package com.example.board.config;

import com.example.board.domain.Board;
import com.example.board.domain.Category;
import com.example.board.domain.User;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CategoryRepository;
import com.example.board.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder; // PasswordEncoder 주입
    private final CategoryRepository categoryRepository;

    public DataLoader(BoardRepository boardRepository, UserRepository userRepository, ObjectMapper objectMapper, PasswordEncoder passwordEncoder,CategoryRepository categoryRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder; // 생성자에 추가
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws IOException {
        loadData("src/main/resources/data/boardData.json", boardRepository, Board.class);
        loadUserData("src/main/resources/data/userData.json", userRepository, User.class); // User 전용 메서드 호출
        loadData("src/main/resources/data/categoryData.json", categoryRepository, Category.class);
    }

    // 데이터 로드 메서드 (Board, Category 등)
    private <T, ID> void loadData(String filePath, JpaRepository<T, ID> repository, Class<T> entityClass) throws IOException {
        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            System.err.println(entityClass.getSimpleName() + " JSON file not found: " + jsonFile.getAbsolutePath());
            return;
        }
        List<T> entities = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
        if (repository.count() == 0) {
            repository.saveAll((Iterable<T>) entities);
            System.out.println("Initial " + entityClass.getSimpleName() + " data loaded into H2 database.");
        } else {
            System.out.println(entityClass.getSimpleName() + " data already exists in the database.");
        }
    }

    // User 데이터 로드 시 비밀번호 암호화 추가
    private void loadUserData(String filePath, UserRepository repository, Class<User> entityClass) throws IOException {
        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            System.err.println("User JSON file not found: " + jsonFile.getAbsolutePath());
            return;
        }
        List<User> users = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
        if (repository.count() == 0) {
            // 비밀번호 암호화 및 역할 확인
            for (User user : users) {
                user.setPw(passwordEncoder.encode(user.getPw())); // 비밀번호 암호화
                if (user.getRole() == null) {
                    user.setRole("ROLE_USER"); // 역할 기본값 설정
                }
            }
            repository.saveAll(users);
            System.out.println("Initial User data loaded into H2 database with encrypted passwords.");
        } else {
            System.out.println("User data already exists in the database.");
        }
    }

}