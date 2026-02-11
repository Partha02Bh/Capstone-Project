package com.example.demo.dto;

import com.example.demo.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
    private String ownerName;
}
