package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.enums.AccountStatus;
import com.example.demo.exception.DuplicateFieldException;
import com.example.demo.exception.DuplicateUsernameException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.InvalidOtpException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private AccountRepository accountRepo;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtUtil jwtUtil;

	public User register(RegisterRequest req) {
		logger.info("Registration attempt for username: {}", req.getUsername());

		// Check for duplicate username
		if (userRepo.findByUsername(req.getUsername()).isPresent()) {
			logger.warn("Registration failed - duplicate username: {}", req.getUsername());
			throw new DuplicateUsernameException(req.getUsername());
		}

		// Check for duplicate email
		if (userRepo.findByEmail(req.getEmail()).isPresent()) {
			logger.warn("Registration failed - duplicate email: {}", req.getEmail());
			throw new DuplicateFieldException("Email", req.getEmail());
		}

		// Check for duplicate phone number
		if (userRepo.findByPhone(req.getPhone()).isPresent()) {
			logger.warn("Registration failed - duplicate phone: {}", req.getPhone());
			throw new DuplicateFieldException("Phone number", req.getPhone());
		}

		User user = new User();
		user.setUsername(req.getUsername());
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setFullName(req.getFullName());
		user.setEmail(req.getEmail());
		user.setPhone(req.getPhone());
		user.setRole("ROLE_USER");
		user.setIsActive(true);

		User savedUser = userRepo.save(user);
		logger.info("User registered successfully: username={}, id={}", savedUser.getUsername(), savedUser.getId());

		Account account = new Account();
		account.setUser(savedUser);
		account.setBalance(BigDecimal.ZERO);
		account.setAccountType("SAVINGS");
		account.setStatus(AccountStatus.ACTIVE);
		String randomAccNum = "10" + (long) (Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L);
		account.setAccountNumber(randomAccNum);

		accountRepo.save(account);
		logger.info("Account created for user: username={}, accountNumber={}", savedUser.getUsername(), randomAccNum);

		return savedUser;
	}

	public String generateOtp(LoginRequest req) {
		logger.info("Login attempt for username: {}", req.getUsername());

		User user = userRepo.findByUsername(req.getUsername())
				.orElseThrow(() -> {
					logger.warn("Login failed - user not found: {}", req.getUsername());
					return new UserNotFoundException("User not found with username: " + req.getUsername());
				});

		if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
			logger.warn("Login failed - invalid password for username: {}", req.getUsername());
			throw new InvalidCredentialsException("Invalid Password");
		}

		String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
		user.setOtpCode(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		userRepo.save(user);

		logger.info("OTP generated for username: {} | OTP: {}", req.getUsername(), otp);
		return "OTP Sent";
	}

	public Map<String, Object> verifyOtp(String username, String otp) {
		logger.info("OTP verification attempt for username: {}", username);

		User user = userRepo.findByUsername(username)
				.orElseThrow(() -> {
					logger.warn("OTP verification failed - user not found: {}", username);
					return new UserNotFoundException("User not found with username: " + username);
				});

		if (user.getOtpCode() != null && user.getOtpCode().equals(otp)) {

			user.setOtpCode(null);
			userRepo.save(user);

			String token = jwtUtil.generateToken(user.getUsername());

			Map<String, Object> response = new HashMap<>();
			response.put("token", token);
			response.put("role", user.getRole());
			response.put("userId", user.getId());

			logger.info("OTP verified successfully for username: {}, role: {}", username, user.getRole());
			return response;
		} else {
			logger.warn("OTP verification failed - invalid OTP for username: {}", username);
			throw new InvalidOtpException();
		}
	}
}