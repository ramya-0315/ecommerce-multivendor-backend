package com.ramyastore.service.impl;

import com.ramyastore.model.VerificationCode;
import com.ramyastore.repository.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificationServiceImplTest {

    private VerificationCodeRepository verificationCodeRepository;
    private VerificationServiceImpl verificationService;

    @BeforeEach
    void setUp() {
        verificationCodeRepository = mock(VerificationCodeRepository.class);
        verificationService = new VerificationServiceImpl(verificationCodeRepository);
    }

    @Test
    void createVerificationCode_WhenNoExistingCode_SavesNewCode() {
        String email = "test@example.com";
        String otp = "123456";

        when(verificationCodeRepository.findByEmail(email)).thenReturn(null);

        VerificationCode savedCode = new VerificationCode();
        savedCode.setEmail(email);
        savedCode.setOtp(otp);
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(savedCode);

        VerificationCode result = verificationService.createVerificationCode(otp, email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(otp, result.getOtp());

        verify(verificationCodeRepository, never()).delete(any());
        verify(verificationCodeRepository).save(any(VerificationCode.class));
    }

    @Test
    void createVerificationCode_WhenExistingCode_DeletesOldAndSavesNew() {
        String email = "test@example.com";
        String otp = "654321";

        VerificationCode existingCode = new VerificationCode();
        existingCode.setEmail(email);
        existingCode.setOtp("oldOtp");

        when(verificationCodeRepository.findByEmail(email)).thenReturn(existingCode);

        VerificationCode savedCode = new VerificationCode();
        savedCode.setEmail(email);
        savedCode.setOtp(otp);
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(savedCode);

        VerificationCode result = verificationService.createVerificationCode(otp, email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(otp, result.getOtp());

        // Verify that the old code was deleted
        verify(verificationCodeRepository).delete(existingCode);
        verify(verificationCodeRepository).save(any(VerificationCode.class));
    }
}
