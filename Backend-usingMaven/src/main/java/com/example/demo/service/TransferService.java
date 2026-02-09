package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.TransactionLog;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionLogRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferService {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private TransactionLogRepository txnRepo; 

    @Transactional
    public TransactionLog transfer(Long sourceId, Long targetId, BigDecimal amount, String key) {
        
  
        if (txnRepo.findByIdempotencyKey(key).isPresent()) {
            return txnRepo.findByIdempotencyKey(key).get();
        }

        Account source = accountRepo.findById(sourceId).orElseThrow();
        Account target = accountRepo.findById(targetId).orElseThrow();

        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient Funds");
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        accountRepo.save(source);
        accountRepo.save(target);

        TransactionLog log = new TransactionLog();
        log.setAccountId(source.getId());
        log.setRelatedAccountId(target.getId());
        log.setAmount(amount);
        log.setTransactionType("TRANSFER");
        log.setTimestamp(LocalDateTime.now());
        log.setIdempotencyKey(key);

        return txnRepo.save(log);
    }
}
