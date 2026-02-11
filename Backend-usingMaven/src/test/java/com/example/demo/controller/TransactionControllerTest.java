package com.example.demo.controller;

import com.example.demo.entity.Account;
import com.example.demo.entity.TransactionLog;
import com.example.demo.enums.AccountStatus;
import com.example.demo.exception.AccountNotActiveException;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientFundsException;
import com.example.demo.exception.InvalidAmountException;
import com.example.demo.exception.SelfTransferException;
import com.example.demo.repositories.AccountRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(TransactionControllerTest.class);

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private TransactionLogRepository transactionLogRepo;

    @InjectMocks
    private TransactionController transactionController;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        // Text Block (Java 15+)
        logger.info("""
                ========== Setting up TransactionController test data ==========
                Source Account: id=1, balance=5000.00, status=ACTIVE
                Target Account: id=2, balance=3000.00, status=ACTIVE
                """);

        sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("1000000001");
        sourceAccount.setBalance(new BigDecimal("5000.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);

        targetAccount = new Account();
        targetAccount.setId(2L);
        targetAccount.setAccountNumber("1000000002");
        targetAccount.setBalance(new BigDecimal("3000.00"));
        targetAccount.setStatus(AccountStatus.ACTIVE);
    }

    // ==================== DEPOSIT TESTS ====================

    @Test
    @DisplayName("Deposit - Success: Should deposit amount and update balance")
    void deposit_Success() {
        logger.info("TEST: Deposit - Success");

        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.save(any(Account.class))).thenReturn(sourceAccount);
        when(transactionLogRepo.save(any(TransactionLog.class))).thenReturn(new TransactionLog());

        Map<String, String> request = Map.of("userId", "1", "amount", "1000");
        ResponseEntity<?> response = transactionController.deposit(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Deposit Successful", response.getBody());
        assertEquals(new BigDecimal("6000.00"), sourceAccount.getBalance());

        logger.info("TEST PASSED: Deposit successful, new balance={}", sourceAccount.getBalance());
    }

    @Test
    @DisplayName("Deposit - Fail: Should throw InvalidAmountException when amount exceeds $10,000")
    void deposit_ExceedsMaximum() {
        logger.info("TEST: Deposit - Exceeds Maximum");

        Map<String, String> request = Map.of("userId", "1", "amount", "15000");

        InvalidAmountException ex = assertThrows(InvalidAmountException.class,
                () -> transactionController.deposit(request));

        assertTrue(ex.getMessage().contains("10,000"));
        logger.info("TEST PASSED: InvalidAmountException thrown: {}", ex.getMessage());
    }

    @Test
    @DisplayName("Deposit - Fail: Should throw InvalidAmountException for zero amount")
    void deposit_ZeroAmount() {
        logger.info("TEST: Deposit - Zero Amount");

        Map<String, String> request = Map.of("userId", "1", "amount", "0");

        assertThrows(InvalidAmountException.class, () -> transactionController.deposit(request));
        logger.info("TEST PASSED: InvalidAmountException thrown for zero amount");
    }

    @Test
    @DisplayName("Deposit - Fail: Should throw AccountNotFoundException for invalid user")
    void deposit_AccountNotFound() {
        logger.info("TEST: Deposit - Account Not Found");

        when(accountRepo.findByUser_Id(99L)).thenReturn(Optional.empty());

        Map<String, String> request = Map.of("userId", "99", "amount", "500");

        assertThrows(AccountNotFoundException.class, () -> transactionController.deposit(request));
        logger.info("TEST PASSED: AccountNotFoundException thrown for userId=99");
    }

    @Test
    @DisplayName("Deposit - Fail: Should throw AccountNotActiveException for LOCKED account")
    void deposit_AccountLocked() {
        logger.info("""
                TEST: Deposit - Account Locked
                Scenario: Account status=LOCKED, attempting deposit
                Expected: AccountNotActiveException thrown
                """);

        sourceAccount.setStatus(AccountStatus.LOCKED);
        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));

        Map<String, String> request = Map.of("userId", "1", "amount", "500");

        AccountNotActiveException ex = assertThrows(AccountNotActiveException.class,
                () -> transactionController.deposit(request));
        assertTrue(ex.getMessage().contains("LOCKED"));

        logger.info("TEST PASSED: AccountNotActiveException thrown: {}", ex.getMessage());
    }

    // ==================== WITHDRAW TESTS ====================

    @Test
    @DisplayName("Withdraw - Success: Should withdraw amount and update balance")
    void withdraw_Success() {
        logger.info("TEST: Withdraw - Success");

        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.save(any(Account.class))).thenReturn(sourceAccount);
        when(transactionLogRepo.save(any(TransactionLog.class))).thenReturn(new TransactionLog());

        Map<String, String> request = Map.of("userId", "1", "amount", "1000");
        ResponseEntity<?> response = transactionController.withdraw(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Withdrawal Successful", response.getBody());
        assertEquals(new BigDecimal("4000.00"), sourceAccount.getBalance());

        logger.info("TEST PASSED: Withdrawal successful, new balance={}", sourceAccount.getBalance());
    }

    @Test
    @DisplayName("Withdraw - Fail: Should throw InsufficientFundsException when balance is low")
    void withdraw_InsufficientFunds() {
        logger.info("TEST: Withdraw - Insufficient Funds");

        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));
        when(transactionLogRepo.save(any(TransactionLog.class))).thenReturn(new TransactionLog());

        Map<String, String> request = Map.of("userId", "1", "amount", "9000");

        assertThrows(InsufficientFundsException.class, () -> transactionController.withdraw(request));
        logger.info("TEST PASSED: InsufficientFundsException thrown for amount > balance");
    }

    @Test
    @DisplayName("Withdraw - Fail: Should throw InvalidAmountException when below $100 minimum")
    void withdraw_BelowMinimum() {
        logger.info("TEST: Withdraw - Below Minimum");

        Map<String, String> request = Map.of("userId", "1", "amount", "50");

        InvalidAmountException ex = assertThrows(InvalidAmountException.class,
                () -> transactionController.withdraw(request));

        assertTrue(ex.getMessage().contains("100"));
        logger.info("TEST PASSED: InvalidAmountException thrown: {}", ex.getMessage());
    }

    @Test
    @DisplayName("Withdraw - Fail: Should throw InvalidAmountException when above $10,000 maximum")
    void withdraw_AboveMaximum() {
        logger.info("TEST: Withdraw - Above Maximum");

        Map<String, String> request = Map.of("userId", "1", "amount", "15000");

        InvalidAmountException ex = assertThrows(InvalidAmountException.class,
                () -> transactionController.withdraw(request));

        assertTrue(ex.getMessage().contains("10,000"));
        logger.info("TEST PASSED: InvalidAmountException thrown: {}", ex.getMessage());
    }

    @Test
    @DisplayName("Withdraw - Fail: Should throw AccountNotActiveException for CLOSED account")
    void withdraw_AccountClosed() {
        logger.info("""
                TEST: Withdraw - Account Closed
                Scenario: Account status=CLOSED, attempting withdrawal
                Expected: AccountNotActiveException thrown
                """);

        sourceAccount.setStatus(AccountStatus.CLOSED);
        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));

        Map<String, String> request = Map.of("userId", "1", "amount", "500");

        AccountNotActiveException ex = assertThrows(AccountNotActiveException.class,
                () -> transactionController.withdraw(request));
        assertTrue(ex.getMessage().contains("CLOSED"));

        logger.info("TEST PASSED: AccountNotActiveException thrown: {}", ex.getMessage());
    }

    // ==================== TRANSFER TESTS ====================

    @Test
    @DisplayName("Transfer - Success: Should transfer amount between two accounts")
    void transfer_Success() {
        logger.info("TEST: Transfer - Success");

        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findByUser_Id(2L)).thenReturn(Optional.of(targetAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionLogRepo.save(any(TransactionLog.class))).thenReturn(new TransactionLog());

        Map<String, String> request = Map.of("sourceId", "1", "targetId", "2", "amount", "1000");
        ResponseEntity<?> response = transactionController.transfer(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Transfer Successful", response.getBody());
        assertEquals(new BigDecimal("4000.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("4000.00"), targetAccount.getBalance());

        logger.info("TEST PASSED: Transfer successful, source={}, target={}", sourceAccount.getBalance(),
                targetAccount.getBalance());
    }

    @Test
    @DisplayName("Transfer - Fail: Should throw SelfTransferException for self-transfer")
    void transfer_SelfTransfer() {
        logger.info("TEST: Transfer - Self Transfer");

        Map<String, String> request = Map.of("sourceId", "1", "targetId", "1", "amount", "500");

        assertThrows(SelfTransferException.class, () -> transactionController.transfer(request));
        logger.info("TEST PASSED: SelfTransferException thrown for self-transfer");
    }

    @Test
    @DisplayName("Transfer - Fail: Should throw InvalidAmountException when below $100")
    void transfer_BelowMinimum() {
        logger.info("TEST: Transfer - Below Minimum");

        Map<String, String> request = Map.of("sourceId", "1", "targetId", "2", "amount", "50");

        assertThrows(InvalidAmountException.class, () -> transactionController.transfer(request));
        logger.info("TEST PASSED: InvalidAmountException thrown for amount below minimum");
    }

    @Test
    @DisplayName("Transfer - Fail: Should throw InvalidAmountException when above $10,000")
    void transfer_AboveMaximum() {
        logger.info("TEST: Transfer - Above Maximum");

        Map<String, String> request = Map.of("sourceId", "1", "targetId", "2", "amount", "15000");

        assertThrows(InvalidAmountException.class, () -> transactionController.transfer(request));
        logger.info("TEST PASSED: InvalidAmountException thrown for amount above maximum");
    }

    @Test
    @DisplayName("Transfer - Fail: Should throw InsufficientFundsException when balance is low")
    void transfer_InsufficientFunds() {
        logger.info("TEST: Transfer - Insufficient Funds");

        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findByUser_Id(2L)).thenReturn(Optional.of(targetAccount));
        when(transactionLogRepo.save(any(TransactionLog.class))).thenReturn(new TransactionLog());

        Map<String, String> request = Map.of("sourceId", "1", "targetId", "2", "amount", "9000");

        assertThrows(InsufficientFundsException.class, () -> transactionController.transfer(request));
        logger.info("TEST PASSED: InsufficientFundsException thrown for transfer exceeding balance");
    }

    @Test
    @DisplayName("Transfer - Fail: Should throw AccountNotFoundException for invalid receiver")
    void transfer_ReceiverNotFound() {
        logger.info("TEST: Transfer - Receiver Not Found");

        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findByUser_Id(99L)).thenReturn(Optional.empty());
        when(transactionLogRepo.save(any(TransactionLog.class))).thenReturn(new TransactionLog());

        Map<String, String> request = Map.of("sourceId", "1", "targetId", "99", "amount", "500");

        assertThrows(AccountNotFoundException.class, () -> transactionController.transfer(request));
        logger.info("TEST PASSED: AccountNotFoundException thrown for invalid receiver");
    }

    @Test
    @DisplayName("Transfer - Fail: Should throw AccountNotActiveException for LOCKED source")
    void transfer_SourceAccountLocked() {
        logger.info("""
                TEST: Transfer - Source Account Locked
                Scenario: Source account status=LOCKED
                Expected: AccountNotActiveException thrown
                """);

        sourceAccount.setStatus(AccountStatus.LOCKED);
        when(accountRepo.findByUser_Id(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findByUser_Id(2L)).thenReturn(Optional.of(targetAccount));

        Map<String, String> request = Map.of("sourceId", "1", "targetId", "2", "amount", "500");

        AccountNotActiveException ex = assertThrows(AccountNotActiveException.class,
                () -> transactionController.transfer(request));
        assertTrue(ex.getMessage().contains("LOCKED"));

        logger.info("TEST PASSED: AccountNotActiveException thrown: {}", ex.getMessage());
    }
}
