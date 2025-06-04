package com.ramyastore.repository;

import com.ramyastore.model.Cart;
import com.ramyastore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ramyastore.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {


    CartItem findByCartAndProductAndSize(Cart cart, Product product, String size);


}
