package com.ramyastore.controller;

import com.ramyastore.model.Home;
import com.ramyastore.model.HomeCategory;
import com.ramyastore.service.HomeCategoryService;
import com.ramyastore.service.HomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    private HomeCategoryService homeCategoryService;
    private HomeService homeService;
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        homeCategoryService = mock(HomeCategoryService.class);
        homeService = mock(HomeService.class);
        customerController = new CustomerController(homeCategoryService, homeService);
    }

    @Test
    void testCreateHomeCategories() {
        // Prepare input and mock data
        HomeCategory category1 = new HomeCategory();
        category1.setName("Electronics");

        HomeCategory category2 = new HomeCategory();
        category2.setName("Clothing");

        List<HomeCategory> inputCategories = Arrays.asList(category1, category2);
        List<HomeCategory> savedCategories = Arrays.asList(category1, category2);

        Home homeResponse = new Home();


        // Mock service methods
        when(homeCategoryService.createCategories(inputCategories)).thenReturn(savedCategories);
        when(homeService.creatHomePageData(savedCategories)).thenReturn(homeResponse);

        // Call controller method
        ResponseEntity<Home> response = customerController.createHomeCategories(inputCategories);

        // Verify results
        assertEquals(202, response.getStatusCodeValue());
        assertEquals(homeResponse, response.getBody());

        // Verify interactions
        verify(homeCategoryService, times(1)).createCategories(inputCategories);
        verify(homeService, times(1)).creatHomePageData(savedCategories);
    }

    @Test
    void testGetHomePageDataReturnsNull() {
        // This method is currently returning null
        ResponseEntity<Home> response = customerController.getHomePageData();
        assertNull(response);
    }
}
