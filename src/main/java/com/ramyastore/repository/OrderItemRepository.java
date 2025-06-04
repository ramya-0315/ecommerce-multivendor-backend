package com.ramyastore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ramyastore.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
