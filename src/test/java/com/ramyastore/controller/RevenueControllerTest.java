package com.ramyastore.controller;

import com.ramyastore.dto.RevenueChart;
import com.ramyastore.exception.SellerException;
import com.ramyastore.model.Seller;
import com.ramyastore.service.RevenueService;
import com.ramyastore.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class RevenueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RevenueService revenueService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private RevenueController revenueController;

    private final String AUTH_HEADER = "Bearer test.jwt.token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(revenueController).build();
    }

    @Test
    void getRevenueChart_returnsRevenueList() throws Exception {
        Seller mockSeller = new Seller();
        mockSeller.setId(1L);

        RevenueChart chart = new RevenueChart();
        // populate chart as needed

        List<RevenueChart> revenueList = singletonList(chart);

        when(sellerService.getSellerProfile(AUTH_HEADER)).thenReturn(mockSeller);
        when(revenueService.getRevenueChartByType("today", 1L)).thenReturn(revenueList);

        mockMvc.perform(get("/api/seller/revenue/chart")
                        .header("Authorization", AUTH_HEADER)
                        .param("type", "today")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void getRevenueChart_usesDefaultTypeToday() throws Exception {
        Seller mockSeller = new Seller();
        mockSeller.setId(2L);

        when(sellerService.getSellerProfile(AUTH_HEADER)).thenReturn(mockSeller);
        when(revenueService.getRevenueChartByType("today", 2L)).thenReturn(singletonList(new RevenueChart()));

        mockMvc.perform(get("/api/seller/revenue/chart")
                        .header("Authorization", AUTH_HEADER)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getRevenueChart_throwsSellerException() throws Exception {
        when(sellerService.getSellerProfile(AUTH_HEADER)).thenThrow(new SellerException("Seller not found"));

        mockMvc.perform(get("/api/seller/revenue/chart")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isInternalServerError());  // you can customize error handling
    }
}
