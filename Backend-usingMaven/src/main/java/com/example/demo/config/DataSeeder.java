package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.demo.repositories.AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("========== Starting database seeding ==========");
        seedUser("admin", "admin123", "ROLE_ADMIN", "Bank Manager", "admin@bank.com", "9000000000", "1000000000",
                new java.math.BigDecimal("0.00"));
        seedUser("Partha", "Partha123", "ROLE_USER", "Partha", "partha@gmail.com", "9000000001", "1000000001",
                new java.math.BigDecimal("500000.00"));
        seedUser("Prakhar", "Prakhar123", "ROLE_USER", "Prakhar", "prakhar@gmail.com", "9000000002", "1000000002",
                new java.math.BigDecimal("300000.00"));
        seedUser("Nidhi", "Nidhi123", "ROLE_USER", "Nidhi", "nidhi@gmail.com", "9000000003", "1000000003",
                new java.math.BigDecimal("400000.00"));
        seedUser("Krishna", "Krishna123", "ROLE_USER", "Krishna", "krishna@gmail.com", "9000000004", "1000000004",
                new java.math.BigDecimal("200000.00"));
        logger.info("========== Database seeding completed ==========");
    }

    private void seedUser(String username, String rawPassword, String role, String fullName, String email, String phone,
            String accountNumber, java.math.BigDecimal balance) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setIsActive(true);
            user = userRepository.save(user);
            logger.info("Created user: username={}, role={}", username, role);

            createAccountForUser(user, accountNumber, balance);
        } else {
            // Update role or password if it doesn't match
            boolean changed = false;

            if (!user.getRole().equals(role)) {
                user.setRole(role);
                changed = true;
                logger.info("Updated role for user: username={}, newRole={}", username, role);
            }

            // Check password (only if not already encrypted match)
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                changed = true;
                logger.info("Updated password for user: username={}", username);
            }

            // Check phone
            if (user.getPhone() == null || !user.getPhone().equals(phone)) {
                user.setPhone(phone);
                changed = true;
            }

            if (changed) {
                userRepository.save(user);
            }
        }
    }

    private void createAccountForUser(User user, String accountNumber, java.math.BigDecimal balance) {
        com.example.demo.entity.Account account = accountRepository.findByUser_Id(user.getId()).orElse(null);

        if (account == null) {
            logger.info("Account not found for user: {}. Creating new account...", user.getUsername());
            account = new com.example.demo.entity.Account();
            account.setUser(user);
            account.setBalance(balance);
            account.setAccountType("SAVINGS");
            account.setStatus(com.example.demo.enums.AccountStatus.ACTIVE);
            account.setAccountNumber(accountNumber);

            accountRepository.save(account);
            logger.info("Created account for user: username={}, accountNumber={}, balance={}", user.getUsername(),
                    accountNumber, balance);
        } else {
            logger.debug("Account found for user: username={}, currentBalance={}", user.getUsername(),
                    account.getBalance());

            // FORCE UPDATE for debugging
            account.setBalance(balance);
            account.setAccountNumber(accountNumber);
            accountRepository.save(account);
            logger.info("Updated account for user: username={}, newBalance={}", user.getUsername(), balance);
        }
    }
}