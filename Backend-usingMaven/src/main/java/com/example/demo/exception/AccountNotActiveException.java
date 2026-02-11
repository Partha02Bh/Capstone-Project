package com.example.demo.exception;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String accountStatus) {
        super("Account is " + accountStatus + " and cannot be used for transactions");
    }

    public AccountNotActiveException(Long accountId, String accountStatus) {
        super("Account ID " + accountId + " is " + accountStatus + " and cannot be used for transactions");
    }
}
