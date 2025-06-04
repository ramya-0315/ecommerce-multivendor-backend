package com.ramyastore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ramyastore.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

	 Cart findByUserId(Long userId);
}
