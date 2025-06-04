package com.ramyastore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ramyastore.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
	PasswordResetToken findByToken(String token);
}
