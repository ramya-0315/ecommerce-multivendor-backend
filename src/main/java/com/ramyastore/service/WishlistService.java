package com.ramyastore.service;


import com.ramyastore.exception.WishlistNotFoundException;
import com.ramyastore.model.Product;
import com.ramyastore.model.User;
import com.ramyastore.model.Wishlist;

public interface WishlistService {

    Wishlist createWishlist(User user);

    Wishlist getWishlistByUserId(User user);

    Wishlist addProductToWishlist(User user, Product product) throws WishlistNotFoundException;

}

