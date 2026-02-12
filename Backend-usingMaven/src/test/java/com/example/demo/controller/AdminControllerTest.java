package com.example.demo.controller;

import com.example.demo.entity.TransactionLog;
import com.example.demo.entity.User;
import com.example.demo.repositories.TransactionLogRepository;
import com.example.demo.repositories.UserRepository;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AdminControllerTest.class);

    @Mock
    private UserRepository userRepo;

    @Mock
    private TransactionLogRepository transactionLogRepo;

    @InjectMocks
    private AdminController adminController;

    private User user1;
    private User user2;
    private TransactionLog txLog1;
    private TransactionLog txLog2;

    @BeforeEach
    void setUp() {
        logger.info("========== Setting up test data ==========");

        user1 = new User();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setFullName("Bank Manager");
        user1.setRole("ROLE_ADMIN");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("Partha");
        user2.setFullName("Partha");
        user2.setRole("ROLE_USER");

        txLog1 = new TransactionLog();
        txLog1.setId(1L);
        txLog1.setAccountId(1L);
        txLog1.setAmount(new BigDecimal("1000"));
        txLog1.setTransactionType("DEPOSIT");
        txLog1.setStatus("SUCCESS");
        txLog1.setTimestamp(LocalDateTime.now());

        txLog2 = new TransactionLog();
        txLog2.setId(2L);
        txLog2.setAccountId(2L);
        txLog2.setAmount(new BigDecimal("-500"));
        txLog2.setTransactionType("WITHDRAW");
        txLog2.setStatus("SUCCESS");
        txLog2.setTimestamp(LocalDateTime.now());

        logger.info("Test data initialized: 2 users, 2 transaction logs");
    }

    // ==================== GET ALL USERS TESTS ====================

    @Test
    @DisplayName("Get All Users - Success: Should return list of all users")
    void getAllUsers_Success() {
        logger.info("TEST: Get All Users - Success");

        when(userRepo.findAll()).thenReturn(Arrays.asList(user1, user2));

        ResponseEntity<List<User>> response = adminController.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("admin", response.getBody().get(0).getUsername());
        assertEquals("Partha", response.getBody().get(1).getUsername());

        logger.info("TEST PASSED: Retrieved {} users", response.getBody().size());
    }

    @Test
    @DisplayName("Get All Users - Empty: Should return empty list when no users")
    void getAllUsers_Empty() {
        logger.info("TEST: Get All Users - Empty");

        when(userRepo.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<User>> response = adminController.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        logger.info("TEST PASSED: Returned empty user list");
    }

    // ==================== GET ALL TRANSACTIONS TESTS ====================

    @Test
    @DisplayName("Get All Transactions - Success")
    void getAllTransactions_Success() {
        // Arrange
        TransactionLog t1 = new TransactionLog();
        t1.setId(1L);
        TransactionLog t2 = new TransactionLog();
        t2.setId(2L);

        when(transactionLogRepo.findAll()).thenReturn(Arrays.asList(t1, t2));

        // Act
        ResponseEntity<List<TransactionLog>> response = adminController.getAllTransactions();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Get All Transactions - Empty")
    void getAllTransactions_Empty() {

        when(transactionLogRepo.findAll()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<TransactionLog>> response = adminController.getAllTransactions();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        logger.info("TEST PASSED: Returned empty transaction list");
    }
}
