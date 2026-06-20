package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sent the transfer (sender earns the reward)
    private Long userId;

    // Source account involved in the transfer
    private Long accountId;

    // Linked to the TRANSFER_OUT transaction log entry
    private Long transactionId;

    // The original transfer amount (used to calculate points)
    private BigDecimal transactionAmount;

    // Points awarded: floor(amount / 100)
    private int pointsEarned;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime awardedAt;
}