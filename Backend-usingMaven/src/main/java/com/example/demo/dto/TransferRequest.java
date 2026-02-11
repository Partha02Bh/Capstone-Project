package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "Source user ID is required")
    private Long sourceId;

    @NotNull(message = "Target user ID is required")
    private Long targetId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum transfer amount is $100")
    @DecimalMax(value = "10000.00", message = "Maximum transfer amount is $10,000")
    private BigDecimal amount;

    private String idempotencyKey;
}
