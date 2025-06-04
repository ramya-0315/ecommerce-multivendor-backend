package com.ramyastore.controller;

import com.ramyastore.domain.AccountStatus;
import com.ramyastore.exception.SellerException;
import com.ramyastore.model.HomeCategory;
import com.ramyastore.model.Seller;
import com.ramyastore.service.HomeCategoryService;
import com.ramyastore.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private SellerService sellerService;

    @Mock
    private HomeCategoryService homeCategoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateSellerStatus_ShouldReturnUpdatedSeller() throws SellerException {
        Long sellerId = 1L;
        AccountStatus status = AccountStatus.ACTIVE;

        Seller mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setAccountStatus(status);

        when(sellerService.updateSellerAccountStatus(sellerId, status)).thenReturn(mockSeller);

        ResponseEntity<Seller> response = adminController.updateSellerStatus(sellerId, status);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockSeller, response.getBody());

        verify(sellerService, times(1)).updateSellerAccountStatus(sellerId, status);
    }

    @Test
    void updateSellerStatus_ShouldThrowSellerException() throws SellerException {
        Long sellerId = 1L;
        AccountStatus status = AccountStatus.SUSPENDED;

        when(sellerService.updateSellerAccountStatus(sellerId, status))
                .thenThrow(new SellerException("Seller not found"));

        SellerException exception = assertThrows(SellerException.class, () -> {
            adminController.updateSellerStatus(sellerId, status);
        });

        assertEquals("Seller not found", exception.getMessage());
        verify(sellerService, times(1)).updateSellerAccountStatus(sellerId, status);
    }

    @Test
    void getHomeCategory_ShouldReturnListOfCategories() throws Exception {
        HomeCategory cat1 = new HomeCategory();
        cat1.setId(1L);
        cat1.setName("Category 1");

        HomeCategory cat2 = new HomeCategory();
        cat2.setId(2L);
        cat2.setName("Category 2");

        List<HomeCategory> categories = Arrays.asList(cat1, cat2);

        when(homeCategoryService.getAllCategories()).thenReturn(categories);

        ResponseEntity<List<HomeCategory>> response = adminController.getHomeCategory();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(categories, response.getBody());

        verify(homeCategoryService, times(1)).getAllCategories();
    }

    @Test
    void updateHomeCategory_ShouldReturnUpdatedCategory() throws Exception {
        Long categoryId = 1L;
        HomeCategory inputCategory = new HomeCategory();
        inputCategory.setName("Updated Category");

        HomeCategory updatedCategory = new HomeCategory();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Updated Category");

        when(homeCategoryService.updateCategory(inputCategory, categoryId)).thenReturn(updatedCategory);

        ResponseEntity<HomeCategory> response = adminController.updateHomeCategory(categoryId, inputCategory);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(updatedCategory, response.getBody());

        verify(homeCategoryService, times(1)).updateCategory(inputCategory, categoryId);
    }
}
