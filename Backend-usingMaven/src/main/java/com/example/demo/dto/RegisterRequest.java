package com.example.demo.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 25, message = "Full name must be at most 25 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Full name must contain only letters and spaces")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(max = 25, message = "Username must be at most 25 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Username must contain only letters (no numbers or special characters)")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 10, message = "Password must be between 6 and 10 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Size(max = 25, message = "Email must be at most 25 characters")
    @Email(message = "Email must be a valid email address (must contain @)")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+\\d{1,3}\\d{10}$", message = "Phone must be a valid country code followed by 10 digits")
    private String phone;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}