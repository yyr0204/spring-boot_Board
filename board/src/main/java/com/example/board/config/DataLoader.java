package com.example.board.config;

import com.example.board.domain.Board;
import com.example.board.domain.User;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * DataLoader 클래스는 애플리케이션 시작 시 초기 데이터를 JSON 파일에서 읽어와 데이터베이스에 저장하는 역할을 합니다.
 * CommandLineRunner를 구현하여 애플리케이션 실행 후 자동으로 데이터를 로드합니다.
 */
@Component
public class DataLoader implements CommandLineRunner {

    // BoardRepository와 UserRepository는 각각 Board와 User 엔티티를 다루는 JPA 레포지토리
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // ObjectMapper는 JSON 데이터를 객체로 변환하는 데 사용
    private final ObjectMapper objectMapper;

    // 생성자에서 BoardRepository, UserRepository, ObjectMapper를 주입받음
    public DataLoader(BoardRepository boardRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 애플리케이션 실행 시 자동으로 호출되는 메소드.
     * boardData.json과 userData.json 파일에서 데이터를 읽어와 DB에 저장하는 메소드를 호출.
     */
    @Override
    public void run(String... args) throws IOException {
        // "src/main/resources/data/boardData.json" 경로의 JSON 데이터를 Board 객체로 로드하고 DB에 저장
        loadData("src/main/resources/data/boardData.json", boardRepository, Board.class);
        
        // "src/main/resources/data/userData.json" 경로의 JSON 데이터를 User 객체로 로드하고 DB에 저장
        loadData("src/main/resources/data/userData.json", userRepository, User.class);
    }

    /**
     * JSON 파일에서 데이터를 읽어와 해당 엔티티 클래스로 변환하여 DB에 저장하는 공통 메소드.
     *
     * @param filePath      : JSON 파일 경로
     * @param repository    : 엔티티를 다루는 JPA 레포지토리(BoardRepository, UserRepository 등)
     * @param entityClass   : 데이터를 변환할 엔티티 클래스(Board.class, User.class 등)
     * @param <T>           : 엔티티 클래스의 타입
     * @param <ID>          : 엔티티 ID 타입
     * @throws IOException  : 파일 읽기 또는 JSON 변환 중 오류 발생 시 예외 처리
     */
    private <T, ID> void loadData(String filePath, JpaRepository<T, ID> repository, Class<T> entityClass) throws IOException {
        // 지정된 경로의 JSON 파일 객체 생성
        File jsonFile = new File(filePath);

        // 파일이 존재하지 않으면 에러 메시지 출력하고 메소드 종료
        if (!jsonFile.exists()) {
            System.err.println(entityClass.getSimpleName() + " JSON file not found: " + jsonFile.getAbsolutePath());
            return;
        }

        // JSON 파일을 해당 엔티티 클래스 객체 리스트로 변환
        List<T> entities = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));

        // DB에 데이터가 없으면(0개일 경우) JSON 파일에서 로드한 데이터를 DB에 저장
        if (repository.count() == 0) {
            repository.saveAll((Iterable<T>) entities); // List<T>를 Iterable<T>로 캐스팅하여 저장
            System.out.println("Initial " + entityClass.getSimpleName() + " data loaded into H2 database.");
        } else {
            // 이미 데이터가 존재하면 DB에 저장하지 않음
            System.out.println(entityClass.getSimpleName() + " data already exists in the database.");
        }
    }
}
