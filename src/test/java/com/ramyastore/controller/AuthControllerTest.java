package com.ramyastore.controller;

import com.ramyastore.domain.USER_ROLE;
import com.ramyastore.exception.SellerException;
import com.ramyastore.exception.UserException;
import com.ramyastore.model.VerificationCode;
import com.ramyastore.request.LoginRequest;
import com.ramyastore.request.SignupRequest;
import com.ramyastore.response.ApiResponse;
import com.ramyastore.response.AuthResponse;
import com.ramyastore.service.AuthService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sentLoginOtp_ShouldReturnSuccessResponse() throws MessagingException, UserException {
        VerificationCode request = new VerificationCode();
        request.setEmail("test@example.com");

        doNothing().when(authService).sentLoginOtp("test@example.com");

        ResponseEntity<ApiResponse> response = authController.sentLoginOtp(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("otp sent", response.getBody().getMessage());

        verify(authService, times(1)).sentLoginOtp("test@example.com");
    }

    @Test
    void createUserHandler_ShouldReturnAuthResponse() throws SellerException {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setFullName("New User");
        signupRequest.setOtp("123456");

        String mockToken = "jwt-token";

        when(authService.createUser(signupRequest)).thenReturn(mockToken);

        ResponseEntity<AuthResponse> response = authController.createUserHandler(signupRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockToken, response.getBody().getJwt());
        assertEquals("Register Success", response.getBody().getMessage());
        assertEquals(USER_ROLE.ROLE_CUSTOMER, response.getBody().getRole());

        verify(authService, times(1)).createUser(signupRequest);
    }

    @Test
    void signin_ShouldReturnAuthResponse() throws SellerException {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("securepass");

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setJwt("jwt-token");
        mockResponse.setMessage("Login Success");
        mockResponse.setRole(USER_ROLE.ROLE_CUSTOMER);

        when(authService.signin(loginRequest)).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = authController.signin(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());

        verify(authService, times(1)).signin(loginRequest);
    }
}
