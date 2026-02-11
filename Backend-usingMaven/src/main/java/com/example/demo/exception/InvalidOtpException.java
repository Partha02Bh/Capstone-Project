package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException() {
        super("Invalid or expired OTP");
    }

    public InvalidOtpException(String message) {
        super(message);
    }
}
