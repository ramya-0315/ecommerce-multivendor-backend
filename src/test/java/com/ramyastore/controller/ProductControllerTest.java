package com.ramyastore.controller;

import com.ramyastore.exception.ProductException;
import com.ramyastore.model.Product;
import com.ramyastore.service.ProductService;
import com.ramyastore.service.SellerService;
import com.ramyastore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private MockMvc mockMvc;

    private ProductService productService;

    private UserService userService;

    private SellerService sellerService;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productService = Mockito.mock(ProductService.class);
        userService = Mockito.mock(UserService.class);
        sellerService = Mockito.mock(SellerService.class);

        productController = new ProductController(productService, userService, sellerService);

        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void getProductById_returnsProduct_whenProductExists() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setTitle("Test Product");

        when(productService.findProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")));
    }

    @Test
    void getProductById_throwsProductException_whenProductNotFound() throws Exception {
        when(productService.findProductById(1L)).thenThrow(new ProductException("Product not found"));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isInternalServerError());
        // You can customize exception handling to return specific status
    }

    @Test
    void searchProduct_returnsListOfProducts() throws Exception {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setTitle("Test Product 1");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setTitle("Test Product 2");

        List<Product> productList = Arrays.asList(product1, product2);

        when(productService.searchProduct("test")).thenReturn(productList);

        mockMvc.perform(get("/products/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void getAllProducts_returnsPagedProducts() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setTitle("Paged Product");

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productService.getAllProduct(
                anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(), anyString(), anyString(), anyInt()
        )).thenReturn(page);

        mockMvc.perform(get("/products")
                        .param("category", "electronics")
                        .param("brand", "brandA")
                        .param("color", "black")
                        .param("size", "M")
                        .param("minPrice", "100")
                        .param("maxPrice", "1000")
                        .param("minDiscount", "10")
                        .param("sort", "priceAsc")
                        .param("stock", "inStock")
                        .param("pageNumber", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Paged Product")));
    }
}
