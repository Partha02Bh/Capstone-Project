package com.example.demo.controller;

import com.example.demo.entity.Account;
import com.example.demo.entity.TransactionLog;
import com.example.demo.exception.AccountNotActiveException;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.InsufficientFundsException;
import com.example.demo.exception.InvalidAmountException;
import com.example.demo.exception.SelfTransferException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionLogRepository;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

	@Autowired
	private AccountRepository accountRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private TransactionLogRepository transactionLogRepo;

	private static final BigDecimal MIN_DEPOSIT = new BigDecimal("100");
	private static final BigDecimal MAX_DEPOSIT = new BigDecimal("10000");
	private static final BigDecimal MIN_WITHDRAW = new BigDecimal("100");
	private static final BigDecimal MAX_WITHDRAW = new BigDecimal("10000");
	private static final BigDecimal MIN_TRANSFER = new BigDecimal("100");
	private static final BigDecimal MAX_TRANSFER = new BigDecimal("10000");

	@PostMapping("/deposit")
	public ResponseEntity<?> deposit(@RequestBody Map<String, String> request) {
		Long userId = Long.parseLong(request.get("userId"));
		BigDecimal amount = new BigDecimal(request.get("amount"));

		logger.info("Deposit request: userId={}, amount={}", userId, amount);

		// Validate amount
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidAmountException("Deposit amount must be greater than zero");
		}
		if (amount.compareTo(MIN_DEPOSIT) < 0) {
			throw new InvalidAmountException("Minimum deposit amount is $100");
		}
		if (amount.compareTo(MAX_DEPOSIT) > 0) {
			throw new InvalidAmountException("Maximum deposit amount is $10,000");
		}

		Account account = accountRepo.findByUser_Id(userId)
				.orElseThrow(() -> new AccountNotFoundException(userId));

		// Enhanced switch for account status validation
		validateAccountActive(account);

		account.setBalance(account.getBalance().add(amount));
		accountRepo.save(account);

		logTransaction(account.getId(), amount, "DEPOSIT", null, "SUCCESS", null);
		logger.info("Deposit successful: userId={}, amount={}, newBalance={}", userId, amount, account.getBalance());
		return ResponseEntity.ok("Deposit Successful");
	}

	@PostMapping("/withdraw")
	public ResponseEntity<?> withdraw(@RequestBody Map<String, String> request) {
		Long userId = Long.parseLong(request.get("userId"));
		BigDecimal amount = new BigDecimal(request.get("amount"));

		logger.info("Withdrawal request: userId={}, amount={}", userId, amount);

		// Validate amount
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidAmountException("Withdrawal amount must be greater than zero");
		}
		if (amount.compareTo(MIN_WITHDRAW) < 0) {
			throw new InvalidAmountException("Minimum withdrawal amount is $100");
		}
		if (amount.compareTo(MAX_WITHDRAW) > 0) {
			throw new InvalidAmountException("Maximum withdrawal amount is $10,000");
		}

		Account account = accountRepo.findByUser_Id(userId)
				.orElseThrow(() -> new AccountNotFoundException(userId));

		// Enhanced switch for account status validation
		validateAccountActive(account);

		if (account.getBalance().compareTo(amount) < 0) {
			logger.warn("Withdrawal failed - insufficient funds: userId={}, balance={}, requested={}", userId,
					account.getBalance(), amount);
			logTransaction(account.getId(), amount.negate(), "WITHDRAW", null, "FAILED", "Insufficient Funds");
			throw new InsufficientFundsException();
		}

		account.setBalance(account.getBalance().subtract(amount));
		accountRepo.save(account);

		logTransaction(account.getId(), amount.negate(), "WITHDRAW", null, "SUCCESS", null);
		logger.info("Withdrawal successful: userId={}, amount={}, newBalance={}", userId, amount,
				account.getBalance());

		return ResponseEntity.ok("Withdrawal Successful");
	}

	@PostMapping("/transfer")
	public ResponseEntity<?> transfer(@RequestBody Map<String, String> request) {
		Long sourceUserId = Long.parseLong(request.get("sourceId"));
		Long targetUserId = Long.parseLong(request.get("targetId"));
		BigDecimal amount = new BigDecimal(request.get("amount"));

		logger.info("Transfer request: sourceUserId={}, targetUserId={}, amount={}", sourceUserId, targetUserId,
				amount);

		// Self-transfer check
		if (sourceUserId.equals(targetUserId)) {
			throw new SelfTransferException();
		}

		// Validate amount
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidAmountException("Transfer amount must be greater than zero");
		}
		if (amount.compareTo(MIN_TRANSFER) < 0) {
			throw new InvalidAmountException("Minimum transfer amount is $100");
		}
		if (amount.compareTo(MAX_TRANSFER) > 0) {
			throw new InvalidAmountException("Maximum transfer amount is $10,000");
		}

		Account sourceAccount = accountRepo.findByUser_Id(sourceUserId)
				.orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
		Account targetAccount = accountRepo.findByUser_Id(targetUserId)
				.orElseThrow(() -> {
					logTransaction(sourceAccount.getId(), amount.negate(), "TRANSFER_OUT", null, "FAILED",
							"Receiver Account not found");
					return new AccountNotFoundException("Receiver account not found");
				});

		// Enhanced switch for account status validation
		validateAccountActive(sourceAccount);
		validateAccountActive(targetAccount);

		if (sourceAccount.getBalance().compareTo(amount) < 0) {
			logger.warn("Transfer failed - insufficient funds: sourceUserId={}, balance={}, requested={}",
					sourceUserId, sourceAccount.getBalance(), amount);
			logTransaction(sourceAccount.getId(), amount.negate(), "TRANSFER_OUT", targetAccount.getId(), "FAILED",
					"Insufficient Funds");
			throw new InsufficientFundsException();
		}

		sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
		targetAccount.setBalance(targetAccount.getBalance().add(amount));

		accountRepo.save(sourceAccount);
		accountRepo.save(targetAccount);

		logTransaction(sourceAccount.getId(), amount.negate(), "TRANSFER_OUT", targetAccount.getId(), "SUCCESS", null);
		logTransaction(targetAccount.getId(), amount, "TRANSFER_IN", sourceAccount.getId(), "SUCCESS", null);

		logger.info("Transfer successful: from userId={} to userId={}, amount={}", sourceUserId, targetUserId, amount);
		return ResponseEntity.ok("Transfer Successful");
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<?> getTransactions(@PathVariable Long accountId) {
		logger.debug("Fetching transactions for accountId={}", accountId);
		return ResponseEntity.ok(transactionLogRepo.findByAccountId(accountId));
	}

	// Enhanced Switch Expression (Java 14+) for account status validation
	private void validateAccountActive(Account account) {
		String statusMessage = switch (account.getStatus()) {
			case ACTIVE -> null;
			case LOCKED -> "LOCKED";
			case CLOSED -> "CLOSED";
		};

		if (statusMessage != null) {
			logger.warn("Account not active: accountId={}, status={}", account.getId(), statusMessage);
			throw new AccountNotActiveException(account.getId(), statusMessage);
		}
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

		logger.info("Transaction logged: type={}, amount={}, status={}, accountId={}", type, amount, status, accountId);
	}
}