package com.example.demo.controller;

import com.example.demo.entity.Account;
import com.example.demo.entity.TransactionLog;
import com.example.demo.entity.User; // 
import com.example.demo.enums.TransactionStatus;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionLogRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	@Autowired
	private AccountRepository accountRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private TransactionLogRepository transactionLogRepo;

	@PostMapping("/deposit")
	public ResponseEntity<?> deposit(@RequestBody Map<String, String> request) {
		Long userId = Long.parseLong(request.get("userId"));
		BigDecimal amount = new BigDecimal(request.get("amount"));

		try {
			Account account = accountRepo.findByUser_Id(userId)
					.orElseThrow(() -> new RuntimeException("Account not found"));

			account.setBalance(account.getBalance().add(amount));
			accountRepo.save(account);

			logTransaction(account.getId(), amount, "DEPOSIT", null, "SUCCESS", null);
			return ResponseEntity.ok("Deposit Successful");

		} catch (Exception e) {
			// Try to find account ID just for logging if possible, otherwise use 0 or null
			// Here we might not have accountId if findByUser_Id failed.
			// But usually we log failures.
			// Ideally we need looking up account first.
			return ResponseEntity.badRequest().body("Deposit Failed: " + e.getMessage());
		}
	}

	@PostMapping("/withdraw")
	public ResponseEntity<?> withdraw(@RequestBody Map<String, String> request) {
		Long userId = Long.parseLong(request.get("userId"));
		BigDecimal amount = new BigDecimal(request.get("amount"));

		Account account;
		try {
			account = accountRepo.findByUser_Id(userId)
					.orElseThrow(() -> new RuntimeException("Account not found"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Account not found");
		}

		if (account.getBalance().compareTo(amount) < 0) {
			logTransaction(account.getId(), amount.negate(), "WITHDRAW", null, "FAILED", "Insufficient Funds");
			return ResponseEntity.badRequest().body("Insufficient Funds");
		}

		account.setBalance(account.getBalance().subtract(amount));
		accountRepo.save(account);

		logTransaction(account.getId(), amount.negate(), "WITHDRAW", null, "SUCCESS", null);

		return ResponseEntity.ok("Withdrawal Successful");
	}

	@PostMapping("/transfer")
	public ResponseEntity<?> transfer(@RequestBody Map<String, String> request) {
		Long sourceUserId = Long.parseLong(request.get("sourceId"));
		Long targetUserId = Long.parseLong(request.get("targetId"));
		BigDecimal amount = new BigDecimal(request.get("amount"));

		Account sourceAccount = accountRepo.findByUser_Id(sourceUserId).orElse(null);
		Account targetAccount = accountRepo.findByUser_Id(targetUserId).orElse(null);

		if (sourceAccount == null) {
			return ResponseEntity.badRequest().body("Sender Account not found");
		}
		if (targetAccount == null) {
			// Log failure for sender? Or just return error?
			// Generally we log actions on the source account.
			logTransaction(sourceAccount.getId(), amount.negate(), "TRANSFER_OUT", null, "FAILED",
					"Receiver Account not found");
			return ResponseEntity.badRequest().body("Receiver Account not found");
		}

		if (sourceAccount.getBalance().compareTo(amount) < 0) {
			logTransaction(sourceAccount.getId(), amount.negate(), "TRANSFER_OUT", targetAccount.getId(), "FAILED",
					"Insufficient Funds");
			return ResponseEntity.badRequest().body("Insufficient Funds");
		}

		sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
		targetAccount.setBalance(targetAccount.getBalance().add(amount));

		accountRepo.save(sourceAccount);
		accountRepo.save(targetAccount);

		logTransaction(sourceAccount.getId(), amount.negate(), "TRANSFER_OUT", targetAccount.getId(), "SUCCESS", null);
		logTransaction(targetAccount.getId(), amount, "TRANSFER_IN", sourceAccount.getId(), "SUCCESS", null);

		return ResponseEntity.ok("Transfer Successful");
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<?> getTransactions(@PathVariable Long accountId) {
		return ResponseEntity.ok(transactionLogRepo.findByAccountId(accountId));
	}

	private void logTransaction(Long accountId, BigDecimal amount, String type, Long relatedAccountId, String status,
			String reason) {
		TransactionLog log = new TransactionLog();
		log.setAccountId(accountId);
		log.setAmount(amount);
		log.setTransactionType(type);
		log.setTimestamp(LocalDateTime.now());
		log.setRelatedAccountId(relatedAccountId);
		log.setStatus(status);
		log.setReasonCode(reason);

		transactionLogRepo.save(log);

		System.out.println("LOG SAVED: " + type + " | " + amount + " | " + status);
	}
}