package com.example.demo.service;

import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;
import com.example.demo.entity.Account;
import com.example.demo.entity.TransactionLog;
import com.example.demo.exception.AccountNotActiveException;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.DuplicateTransferException;
import com.example.demo.exception.InsufficientFundsException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private TransactionLogRepository txnRepo;

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        logger.info("Processing transfer: sourceId={}, targetId={}, amount={}, key={}",
                request.getSourceId(), request.getTargetId(), request.getAmount(), request.getIdempotencyKey());

        // Check for duplicate transfer using idempotency key
        if (request.getIdempotencyKey() != null) {
            var existingTxn = txnRepo.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingTxn.isPresent()) {
                logger.warn("Duplicate transfer detected for key: {}", request.getIdempotencyKey());
                throw new DuplicateTransferException(request.getIdempotencyKey());
            }
        }

        Account source = accountRepo.findById(request.getSourceId())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));
        Account target = accountRepo.findById(request.getTargetId())
                .orElseThrow(() -> new AccountNotFoundException("Target account not found"));

        // Enhanced switch expression (Java 14+) for account status validation
        validateAccountStatus(source, "Source");
        validateAccountStatus(target, "Target");

        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            logger.warn("Insufficient funds: sourceId={}, balance={}, requested={}",
                    source.getId(), source.getBalance(), request.getAmount());
            throw new InsufficientFundsException();
        }

        source.setBalance(source.getBalance().subtract(request.getAmount()));
        target.setBalance(target.getBalance().add(request.getAmount()));

        accountRepo.save(source);
        accountRepo.save(target);

        TransactionLog log = new TransactionLog();
        log.setAccountId(source.getId());
        log.setRelatedAccountId(target.getId());
        log.setAmount(request.getAmount());
        log.setTransactionType("TRANSFER");
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        log.setIdempotencyKey(request.getIdempotencyKey());

        TransactionLog savedLog = txnRepo.save(log);
        logger.info("Transfer completed: txnId={}, from={} to={}, amount={}",
                savedLog.getId(), source.getId(), target.getId(), request.getAmount());

        return TransferResponse.builder()
                .message("Transfer Successful")
                .transactionId(savedLog.getId())
                .amount(request.getAmount())
                .sourceAccountId(source.getId())
                .targetAccountId(target.getId())
                .status("SUCCESS")
                .timestamp(savedLog.getTimestamp())
                .build();
    }

    // Enhanced Switch Expression (Java 14+) for status-based logic
    private void validateAccountStatus(Account account, String label) {
        String statusMessage = switch (account.getStatus()) {
            case ACTIVE -> null; // Active accounts are valid
            case LOCKED -> label + " account is LOCKED";
            case CLOSED -> label + " account is CLOSED";
        };

        if (statusMessage != null) {
            logger.warn("Account status check failed: accountId={}, status={}", account.getId(), account.getStatus());
            throw new AccountNotActiveException(account.getId(), account.getStatus().name());
        }
    }
}
