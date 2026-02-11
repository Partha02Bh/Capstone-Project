package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String message;
    private Long transactionId;
    private BigDecimal amount;
    private Long sourceAccountId;
    private Long targetAccountId;
    private String status;
    private LocalDateTime timestamp;
}
