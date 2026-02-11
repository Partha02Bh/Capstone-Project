package com.example.demo.entity;

import com.example.demo.enums.AccountStatus;
import com.example.demo.exception.InsufficientFundsException;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data // Generates all Getters, Setters, ToString, etc. automatically
@Table(name = "accounts")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String accountNumber;

	// Added this because AuthService tries to set
	private String accountType;

	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	public void credit(BigDecimal amount) {
		if (this.balance == null)
			this.balance = BigDecimal.ZERO;
		this.balance = this.balance.add(amount);
	}

	public void debit(BigDecimal amount) {
		if (this.balance == null)
			this.balance = BigDecimal.ZERO;
		if (this.balance.compareTo(amount) < 0) {
			throw new InsufficientFundsException();
		}
		this.balance = this.balance.subtract(amount);
	}
}