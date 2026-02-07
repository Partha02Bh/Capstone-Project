package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {
	@Autowired
	private UserRepository userRepo;

	public String login(String username, String password) {
		User user = userRepo.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));
		if(!user.getPassword().equals(password)) throw new RuntimeException("Wrong password");
		
		String otp = String.format("%04d", new Random().nextInt(10000));
		user.setOtpCode(otp);
		user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
		userRepo.save(user);
		return otp;
	}
	
	public User verifyOtp(String username, String otp) {
		User user = userRepo.findByUsername(username).orElseThrow();
		if(user.getOtpCode().equals(otp)&& user.getOtpExpiry().isAfter(LocalDateTime.now())) {
			return user;
		}
		throw new RuntimeException("Invalid OTP");
	}

}
