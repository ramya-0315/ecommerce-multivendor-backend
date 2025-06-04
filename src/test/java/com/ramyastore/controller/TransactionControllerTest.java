package com.ramyastore.controller;

import com.ramyastore.exception.SellerException;
import com.ramyastore.model.Order;
import com.ramyastore.model.Seller;
import com.ramyastore.model.Transaction;
import com.ramyastore.service.SellerService;
import com.ramyastore.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;

    private final String AUTH_HEADER = "Bearer test.jwt.token";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createTransaction_returnsTransaction() throws Exception {
        Order order = new Order();
        Transaction transaction = new Transaction();

        when(transactionService.createTransaction(any(Order.class))).thenReturn(transaction);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(transactionService, times(1)).createTransaction(any(Order.class));
    }

    @Test
    void getTransactionBySeller_returnsTransactionList() throws Exception {
        Seller seller = new Seller();
        List<Transaction> transactions = Collections.singletonList(new Transaction());

        when(sellerService.getSellerProfile(anyString())).thenReturn(seller);
        when(transactionService.getTransactionBySeller(seller)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/seller")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(sellerService, times(1)).getSellerProfile(AUTH_HEADER);
        verify(transactionService, times(1)).getTransactionBySeller(seller);
    }

    @Test
    void getAllTransactions_returnsTransactionList() throws Exception {
        List<Transaction> transactions = Collections.singletonList(new Transaction());

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(transactionService, times(1)).getAllTransactions();
    }
}
