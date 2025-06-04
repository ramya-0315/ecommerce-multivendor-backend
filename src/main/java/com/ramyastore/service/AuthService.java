package com.ramyastore.service;

import com.ramyastore.exception.SellerException;
import com.ramyastore.exception.UserException;
import com.ramyastore.request.LoginRequest;
import com.ramyastore.request.SignupRequest;
import com.ramyastore.response.AuthResponse;
import jakarta.mail.MessagingException;

public interface AuthService {

    void sentLoginOtp(String email) throws UserException, MessagingException;
    String createUser(SignupRequest req) throws SellerException;
    AuthResponse signin(LoginRequest req) throws SellerException;

}
