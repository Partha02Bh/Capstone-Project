package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// Handle @Valid validation errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

		String firstMessage = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(e -> e.getDefaultMessage())
				.orElse("Validation failed");

		logger.warn("Validation error: {}", firstMessage);

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value())
				.message(firstMessage)
				.errors(fieldErrors)
				.build();

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// --- Custom Exception Handlers ---

	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
		logger.warn("Account not found: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
		logger.warn("User not found: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(InsufficientFundsException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
		logger.warn("Insufficient funds: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
		logger.warn("Invalid credentials: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(DuplicateUsernameException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateUsername(DuplicateUsernameException ex) {
		logger.warn("Duplicate username: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(DuplicateFieldException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateField(DuplicateFieldException ex) {
		logger.warn("Duplicate field: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
		logger.warn("Invalid OTP: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(SelfTransferException.class)
	public ResponseEntity<ErrorResponse> handleSelfTransfer(SelfTransferException ex) {
		logger.warn("Self-transfer attempt: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidAmountException.class)
	public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException ex) {
		logger.warn("Invalid amount: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AccountNotActiveException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotActive(AccountNotActiveException ex) {
		logger.warn("Account not active: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(DuplicateTransferException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateTransfer(DuplicateTransferException ex) {
		logger.warn("Duplicate transfer: {}", ex.getMessage());
		return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	// --- Fallback Handlers using Pattern Matching instanceof ---

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
		// Pattern matching instanceof (Java 16+)
		if (ex instanceof IllegalArgumentException illegalArg) {
			logger.warn("Illegal argument: {}", illegalArg.getMessage());
			return buildErrorResponse(illegalArg.getMessage(), HttpStatus.BAD_REQUEST);
		}
		if (ex instanceof IllegalStateException illegalState) {
			logger.error("Illegal state: {}", illegalState.getMessage());
			return buildErrorResponse(illegalState.getMessage(), HttpStatus.CONFLICT);
		}

		logger.error("Unhandled RuntimeException: {}", ex.getMessage(), ex);
		return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
		logger.error("Unexpected internal error: {}", ex.getMessage(), ex);

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.message("An unexpected internal error occurred")
				.details(ex.getMessage())
				.build();

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// --- Helper using ErrorResponse DTO ---
	private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(status.value())
				.message(message)
				.build();

		return new ResponseEntity<>(error, status);
	}
}