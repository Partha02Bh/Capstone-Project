package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Account;
import com.example.demo.entity.User;
import com.example.demo.exception.DuplicateUsernameException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.InvalidOtpException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceTest.class);

    @Mock
    private UserRepository userRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        logger.info("========== Setting up test data ==========");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Partha");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Partha");
        testUser.setEmail("partha@gmail.com");
        testUser.setPhone("+911234567890");
        testUser.setRole("ROLE_USER");
        testUser.setIsActive(true);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("NewUser");
        registerRequest.setPassword("Pass1234");
        registerRequest.setFullName("New User");
        registerRequest.setEmail("newuser@gmail.com");
        registerRequest.setPhone("+911234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("Partha");
        loginRequest.setPassword("Partha123");

        logger.info("Test data initialized: testUser={}, registerRequest={}", testUser.getUsername(),
                registerRequest.getUsername());
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Register - Success: Should register a new user and create account")
    void register_Success() {
        logger.info("TEST: Register - Success");

        when(userRepo.findByUsername("NewUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Pass1234")).thenReturn("encodedPass");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(accountRepo.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("NewUser", result.getUsername());
        assertEquals("ROLE_USER", result.getRole());
        verify(userRepo).save(any(User.class));
        verify(accountRepo).save(any(Account.class));

        logger.info("TEST PASSED: User registered successfully with id={}", result.getId());
    }

    @Test
    @DisplayName("Register - Fail: Should throw DuplicateUsernameException for existing username")
    void register_DuplicateUsername() {
        logger.info("TEST: Register - Duplicate Username");

        when(userRepo.findByUsername("NewUser")).thenReturn(Optional.of(testUser));

        DuplicateUsernameException ex = assertThrows(DuplicateUsernameException.class,
                () -> authService.register(registerRequest));

        assertTrue(ex.getMessage().contains("NewUser"));
        verify(userRepo, never()).save(any());

        logger.info("TEST PASSED: DuplicateUsernameException thrown as expected: {}", ex.getMessage());
    }

    // ==================== GENERATE OTP TESTS ====================

    @Test
    @DisplayName("Login - Success: Should generate OTP for valid credentials")
    void generateOtp_Success() {
        logger.info("TEST: Generate OTP - Success");

        when(userRepo.findByUsername("Partha")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Partha123", "encodedPassword")).thenReturn(true);
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        String result = authService.generateOtp(loginRequest);

        assertEquals("OTP Sent", result);
        verify(userRepo).save(any(User.class));
        assertNotNull(testUser.getOtpCode());

        logger.info("TEST PASSED: OTP generated successfully for user: {}", loginRequest.getUsername());
    }

    @Test
    @DisplayName("Login - Fail: Should throw UserNotFoundException for invalid username")
    void generateOtp_UserNotFound() {
        logger.info("TEST: Generate OTP - User Not Found");

        when(userRepo.findByUsername("Partha")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.generateOtp(loginRequest));

        logger.info("TEST PASSED: UserNotFoundException thrown for username: {}", loginRequest.getUsername());
    }

    @Test
    @DisplayName("Login - Fail: Should throw InvalidCredentialsException for wrong password")
    void generateOtp_InvalidPassword() {
        logger.info("TEST: Generate OTP - Invalid Password");

        when(userRepo.findByUsername("Partha")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Partha123", "encodedPassword")).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.generateOtp(loginRequest));

        assertEquals("Invalid Password", ex.getMessage());

        logger.info("TEST PASSED: InvalidCredentialsException thrown: {}", ex.getMessage());
    }

    // ==================== VERIFY OTP TESTS ====================

    @Test
    @DisplayName("Verify OTP - Success: Should return JWT token for valid OTP")
    void verifyOtp_Success() {
        logger.info("TEST: Verify OTP - Success");

        testUser.setOtpCode("123456");
        testUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepo.findByUsername("Partha")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("Partha")).thenReturn("mock-jwt-token");
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        Map<String, Object> result = authService.verifyOtp("Partha", "123456");

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.get("token"));
        assertEquals("ROLE_USER", result.get("role"));
        assertEquals(1L, result.get("userId"));
        assertNull(testUser.getOtpCode()); // OTP should be cleared

        logger.info("TEST PASSED: OTP verified, JWT token returned: {}", result.get("token"));
    }

    @Test
    @DisplayName("Verify OTP - Fail: Should throw InvalidOtpException for wrong OTP")
    void verifyOtp_InvalidOtp() {
        logger.info("TEST: Verify OTP - Invalid OTP");

        testUser.setOtpCode("123456");
        when(userRepo.findByUsername("Partha")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidOtpException.class, () -> authService.verifyOtp("Partha", "999999"));

        logger.info("TEST PASSED: InvalidOtpException thrown for wrong OTP");
    }

    @Test
    @DisplayName("Verify OTP - Fail: Should throw UserNotFoundException for invalid username")
    void verifyOtp_UserNotFound() {
        logger.info("TEST: Verify OTP - User Not Found");

        when(userRepo.findByUsername("Unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.verifyOtp("Unknown", "123456"));

        logger.info("TEST PASSED: UserNotFoundException thrown for unknown user");
    }
}
