package com.ramyastore.service;

import com.ramyastore.model.VerificationCode;

public interface VerificationService {

    VerificationCode createVerificationCode(String otp, String email);
}
