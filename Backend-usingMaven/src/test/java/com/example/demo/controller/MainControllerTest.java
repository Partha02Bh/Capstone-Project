package com.example.demo.controller;

import com.example.demo.dto.AccountResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.enums.AccountStatus;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(MainControllerTest.class);

    @Mock
    private AuthService authService;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private MainController mainController;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Text Block (Java 15+)
        logger.info("""
                ========== Setting up MainController test data ==========
                User: id=2, username=Partha
                Account: id=1, accountNumber=1000000001, balance=5000.00
                """);

        testUser = new User();
        testUser.setId(2L);
        testUser.setUsername("Partha");
        testUser.setFullName("Partha");
        testUser.setEmail("partha@gmail.com");
        testUser.setRole("ROLE_USER");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("1000000001");
        testAccount.setBalance(new BigDecimal("5000.00"));
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setUser(testUser);
    }

    // ==================== GET ACCOUNT TESTS ====================

    @Test
    @DisplayName("Get Account - Success: Should return AccountResponse DTO")
    void getAccount_Success() {
        logger.info("TEST: Get Account - Success (returns AccountResponse DTO)");

        when(accountRepo.findByUser_Id(2L)).thenReturn(Optional.of(testAccount));

        ResponseEntity<AccountResponse> response = mainController.getAccount(2L);

        assertEquals(200, response.getStatusCode().value());
        AccountResponse result = response.getBody();
        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        assertEquals("1000000001", result.getAccountNumber());
        assertEquals(new BigDecimal("5000.00"), result.getBalance());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals("Partha", result.getOwnerName());

        logger.info("""
                TEST PASSED: AccountResponse returned
                  accountId=%d, accountNumber=%s, balance=%s, status=%s, ownerName=%s
                """.formatted(result.getAccountId(), result.getAccountNumber(),
                result.getBalance(), result.getStatus(), result.getOwnerName()));
    }

    @Test
    @DisplayName("Get Account - Fail: Should throw AccountNotFoundException for invalid userId")
    void getAccount_NotFound() {
        logger.info("TEST: Get Account - Not Found");

        when(accountRepo.findByUser_Id(99L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> mainController.getAccount(99L));
        logger.info("TEST PASSED: AccountNotFoundException thrown for userId=99");
    }

    // ==================== GET USER NAME TESTS ====================

    @Test
    @DisplayName("Get User Name - Success: Should return full name for valid userId")
    void getUserName_Success() {
        logger.info("TEST: Get User Name - Success");

        when(userRepo.findById(2L)).thenReturn(Optional.of(testUser));

        ResponseEntity<?> response = mainController.getUserName(2L);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result);
        assertEquals("Partha", result.get("fullName"));
        assertEquals(2L, result.get("userId"));

        logger.info("TEST PASSED: User name retrieved - fullName={}", result.get("fullName"));
    }

    @Test
    @DisplayName("Get User Name - Fail: Should throw UserNotFoundException for invalid userId")
    void getUserName_NotFound() {
        logger.info("TEST: Get User Name - Not Found");

        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> mainController.getUserName(99L));
        logger.info("TEST PASSED: UserNotFoundException thrown for userId=99");
    }
}
