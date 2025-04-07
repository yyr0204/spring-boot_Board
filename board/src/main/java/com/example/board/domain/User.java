package com.example.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    private String id; 
    @Column(nullable = false)
    private String pw;
    private String email;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}