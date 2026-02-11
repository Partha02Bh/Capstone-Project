package com.example.demo.exception;

public class DuplicateTransferException extends RuntimeException {

    public DuplicateTransferException(String idempotencyKey) {
        super("Duplicate transfer detected. Transaction with key '" + idempotencyKey + "' has already been processed");
    }
}
