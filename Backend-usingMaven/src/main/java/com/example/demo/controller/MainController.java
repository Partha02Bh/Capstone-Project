package com.example.demo.controller;

import com.example.demo.dto.AccountResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;

import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration endpoint called for username: {}", request.getUsername());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Login endpoint called for username: {}", request.getUsername());
        return ResponseEntity.ok(authService.generateOtp(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");
        logger.info("OTP verify endpoint called for username: {}", username);
        return ResponseEntity.ok(authService.verifyOtp(username, otp));
    }

    @GetMapping("/accounts/{userId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable("userId") Long userId) {
        logger.info("Get account endpoint called for userId: {}", userId);

        Account account = accountRepo.findByUser_Id(userId)
                .orElseThrow(() -> new AccountNotFoundException(userId));

        // Return AccountResponse DTO instead of raw entity
        User user = account.getUser();
        com.example.demo.dto.UserResponse userResponse = null;
        if (user != null) {
            userResponse = com.example.demo.dto.UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole())
                    .build();
        }

        AccountResponse response = AccountResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .user(userResponse)
                .build();

        logger.info("Account retrieved: accountId={}, balance={}", account.getId(), account.getBalance());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/name")
    public ResponseEntity<?> getUserName(@PathVariable("userId") Long userId) {
        logger.debug("Get user name endpoint called for userId: {}", userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return ResponseEntity.ok(Map.of("fullName", user.getFullName(), "userId", user.getId()));
    }
}