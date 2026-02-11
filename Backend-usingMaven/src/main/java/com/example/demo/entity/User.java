package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "ROLE_USER" or "ROLE_ADMIN"

    // NEW FIELDS FROM PHASE 1
    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private Boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    // OTP fields (Keep these for now)
    @JsonIgnore
    private String otpCode;
    @JsonIgnore
    private LocalDateTime otpExpiry;
}