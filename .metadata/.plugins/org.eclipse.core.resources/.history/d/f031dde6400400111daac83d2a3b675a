package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.enums.TransactionStatus;
import com.example.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class TransferService {
	@Autowired
	private AccountRepository accountRepo;
	@Autowired
	private TransactionRepository txnRepo;

	@Transactional
	public TransactionLog transfer(Long sourceId, Long targetId, BigDecimal amount, String key) {
		if (txnRepo.findByIdempotencyKey(key).isPresent())
			return null;
		Account source = accountRepo.findById(sourceId).orElseThrow();
		Account target = accountRepo.findById(targetId).orElseThrow();
		source.debit(amount);
		target.credit(amount);
		accountRepo.save(source);
		accountRepo.save(target);

		return txnRepo.save(TransactionLog.builder().sourceAccountId(source.getId()).targetAccountId(target.getId())
				.status(TransactionStatus.SUCCESS).createdAt(LocalDateTime.now()).build());

	}

}
