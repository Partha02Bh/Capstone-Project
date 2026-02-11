package com.example.demo.service;

import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.TransactionLog;
import com.example.demo.enums.AccountStatus;
import com.example.demo.exception.AccountNotActiveException;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.DuplicateTransferException;
import com.example.demo.exception.InsufficientFundsException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(TransferServiceTest.class);

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionLogRepository txnRepo;

    @InjectMocks
    private TransferService transferService;

    private Account sourceAccount;
    private Account targetAccount;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        // Text Block (Java 15+) for multi-line test setup log
        logger.info("""
                ========== Setting up TransferService test data ==========
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

        transferRequest = new TransferRequest();
        transferRequest.setSourceId(1L);
        transferRequest.setTargetId(2L);
        transferRequest.setAmount(new BigDecimal("1000.00"));
        transferRequest.setIdempotencyKey("txn-key-001");
    }

    // ==================== TRANSFER SUCCESS ====================

    @Test
    @DisplayName("Transfer - Success: Should transfer and return TransferResponse")
    void transfer_Success() {
        // Text Block for test description
        logger.info("""
                TEST: Transfer - Success
                Scenario: Valid transfer of $1000 from account 1 to account 2
                Expected: Transfer completes, balances updated, TransferResponse returned
                """);

        when(txnRepo.findByIdempotencyKey("txn-key-001")).thenReturn(Optional.empty());
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(txnRepo.save(any(TransactionLog.class))).thenAnswer(inv -> {
            TransactionLog log = inv.getArgument(0);
            log.setId(100L);
            return log;
        });

        TransferResponse result = transferService.transfer(transferRequest);

        assertNotNull(result);
        assertEquals("Transfer Successful", result.getMessage());
        assertEquals(100L, result.getTransactionId());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        assertEquals(1L, result.getSourceAccountId());
        assertEquals(2L, result.getTargetAccountId());
        assertEquals("SUCCESS", result.getStatus());

        assertEquals(new BigDecimal("4000.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("4000.00"), targetAccount.getBalance());

        logger.info("TEST PASSED: Transfer successful, response={}", result);
    }

    // ==================== DUPLICATE TRANSFER ====================

    @Test
    @DisplayName("Transfer - Fail: Should throw DuplicateTransferException for reused key")
    void transfer_DuplicateKey() {
        logger.info("""
                TEST: Transfer - Duplicate Key
                Scenario: Idempotency key 'txn-key-001' already exists
                Expected: DuplicateTransferException thrown
                """);

        TransactionLog existingLog = new TransactionLog();
        existingLog.setId(50L);
        existingLog.setIdempotencyKey("txn-key-001");
        when(txnRepo.findByIdempotencyKey("txn-key-001")).thenReturn(Optional.of(existingLog));

        DuplicateTransferException ex = assertThrows(DuplicateTransferException.class,
                () -> transferService.transfer(transferRequest));

        assertTrue(ex.getMessage().contains("txn-key-001"));
        verify(accountRepo, never()).findById(any());

        logger.info("TEST PASSED: DuplicateTransferException thrown: {}", ex.getMessage());
    }

    // ==================== INSUFFICIENT FUNDS ====================

    @Test
    @DisplayName("Transfer - Fail: Should throw InsufficientFundsException when balance too low")
    void transfer_InsufficientFunds() {
        logger.info("""
                TEST: Transfer - Insufficient Funds
                Scenario: Source balance=$5000, transfer amount=$9000
                Expected: InsufficientFundsException thrown
                """);

        transferRequest.setAmount(new BigDecimal("9000.00"));
        when(txnRepo.findByIdempotencyKey("txn-key-001")).thenReturn(Optional.empty());
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(targetAccount));

        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(transferRequest));

        logger.info("TEST PASSED: InsufficientFundsException thrown for amount > balance");
    }

    // ==================== ACCOUNT NOT ACTIVE (LOCKED) ====================

    @Test
    @DisplayName("Transfer - Fail: Should throw AccountNotActiveException when source is LOCKED")
    void transfer_SourceAccountLocked() {
        logger.info("""
                TEST: Transfer - Source Account Locked
                Scenario: Source account status=LOCKED
                Expected: AccountNotActiveException thrown
                """);

        sourceAccount.setStatus(AccountStatus.LOCKED);
        when(txnRepo.findByIdempotencyKey("txn-key-001")).thenReturn(Optional.empty());
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(targetAccount));

        AccountNotActiveException ex = assertThrows(AccountNotActiveException.class,
                () -> transferService.transfer(transferRequest));

        assertTrue(ex.getMessage().contains("LOCKED"));

        logger.info("TEST PASSED: AccountNotActiveException thrown: {}", ex.getMessage());
    }

    // ==================== ACCOUNT NOT ACTIVE (CLOSED) ====================

    @Test
    @DisplayName("Transfer - Fail: Should throw AccountNotActiveException when target is CLOSED")
    void transfer_TargetAccountClosed() {
        logger.info("""
                TEST: Transfer - Target Account Closed
                Scenario: Target account status=CLOSED
                Expected: AccountNotActiveException thrown
                """);

        targetAccount.setStatus(AccountStatus.CLOSED);
        when(txnRepo.findByIdempotencyKey("txn-key-001")).thenReturn(Optional.empty());
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(targetAccount));

        AccountNotActiveException ex = assertThrows(AccountNotActiveException.class,
                () -> transferService.transfer(transferRequest));

        assertTrue(ex.getMessage().contains("CLOSED"));

        logger.info("TEST PASSED: AccountNotActiveException thrown: {}", ex.getMessage());
    }

    // ==================== ACCOUNT NOT FOUND ====================

    @Test
    @DisplayName("Transfer - Fail: Should throw AccountNotFoundException for invalid source")
    void transfer_SourceNotFound() {
        logger.info("""
                TEST: Transfer - Source Account Not Found
                Scenario: Source account ID doesn't exist
                Expected: AccountNotFoundException thrown
                """);

        when(txnRepo.findByIdempotencyKey("txn-key-001")).thenReturn(Optional.empty());
        when(accountRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(transferRequest));

        logger.info("TEST PASSED: AccountNotFoundException thrown for source account");
    }

    @Test
    @DisplayName("Transfer - Fail: Should throw AccountNotFoundException for invalid target")
    void transfer_TargetNotFound() {
        logger.info("""
                TEST: Transfer - Target Account Not Found
                Scenario: Target account ID doesn't exist
                Expected: AccountNotFoundException thrown
                """);

        when(txnRepo.findByIdempotencyKey("txn-key-001")).thenReturn(Optional.empty());
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(transferRequest));

        logger.info("TEST PASSED: AccountNotFoundException thrown for target account");
    }

    // ==================== NO IDEMPOTENCY KEY ====================

    @Test
    @DisplayName("Transfer - Success: Should transfer without idempotency key")
    void transfer_NoIdempotencyKey() {
        logger.info("""
                TEST: Transfer - No Idempotency Key
                Scenario: Transfer without providing an idempotency key
                Expected: Transfer completes, idempotency check skipped
                """);

        transferRequest.setIdempotencyKey(null);
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(txnRepo.save(any(TransactionLog.class))).thenAnswer(inv -> {
            TransactionLog log = inv.getArgument(0);
            log.setId(101L);
            return log;
        });

        TransferResponse result = transferService.transfer(transferRequest);

        assertNotNull(result);
        assertEquals("Transfer Successful", result.getMessage());
        verify(txnRepo, never()).findByIdempotencyKey(any());

        logger.info("TEST PASSED: Transfer completed without idempotency key");
    }
}
