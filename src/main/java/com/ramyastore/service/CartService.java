package com.ramyastore.service;

import com.ramyastore.exception.ProductException;
import com.ramyastore.model.Cart;
import com.ramyastore.model.CartItem;
import com.ramyastore.model.Product;
import com.ramyastore.model.User;

public interface CartService {
	
	public CartItem addCartItem(User user,
								Product product,
								String size,
								int quantity) throws ProductException;
	
	public Cart findUserCart(User user);

}
