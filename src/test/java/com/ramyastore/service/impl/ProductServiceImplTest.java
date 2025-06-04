package com.ramyastore.service.impl;

import com.ramyastore.exception.ProductException;
import com.ramyastore.model.Category;
import com.ramyastore.model.Product;
import com.ramyastore.model.Seller;
import com.ramyastore.repository.CategoryRepository;
import com.ramyastore.repository.ProductRepository;
import com.ramyastore.request.CreateProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Seller seller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        seller = new Seller();
        seller.setId(1L);
    }

    @Test
    void createProduct_shouldCreateProductWithCategoriesAndReturnSavedProduct() throws ProductException {
        // Arrange
        CreateProductRequest req = new CreateProductRequest();
        req.setCategory("cat1");
        req.setCategory2("cat2");
        req.setCategory3("cat3");
        req.setTitle("Test Product");
        req.setColor("Red");
        req.setDescription("Description");
        req.setMrpPrice(100);
        req.setSellingPrice(80);
        req.setImages(List.of("img1", "img2"));
        req.setSizes("S,M"); // ✅ if expecting a single String



        when(categoryRepository.findByCategoryId("cat1")).thenReturn(null);
        when(categoryRepository.findByCategoryId("cat2")).thenReturn(null);
        when(categoryRepository.findByCategoryId("cat3")).thenReturn(null);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(productCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product savedProduct = productService.createProduct(req, seller);

        // Assert
        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getTitle());
        assertEquals(seller, savedProduct.getSeller());
        assertEquals(20, savedProduct.getDiscountPercent());
        assertNotNull(savedProduct.getCreatedAt());
        assertEquals("Red", savedProduct.getColor());
        assertEquals(100.0, savedProduct.getMrpPrice());
        assertEquals(80.0, savedProduct.getSellingPrice());
        assertEquals(List.of("img1", "img2"), savedProduct.getImages());
        assertEquals(List.of("S", "M"), savedProduct.getSizes());

        // Check category levels and names
        Category categoryLevel3 = savedProduct.getCategory();
        assertNotNull(categoryLevel3);
        assertEquals(3, categoryLevel3.getLevel());
        assertEquals("cat3", categoryLevel3.getCategoryId());
        assertEquals("cat3".replace("_", " "), categoryLevel3.getName());

        Category categoryLevel2 = categoryLevel3.getParentCategory();
        assertNotNull(categoryLevel2);
        assertEquals(2, categoryLevel2.getLevel());
        assertEquals("cat2", categoryLevel2.getCategoryId());

        Category categoryLevel1 = categoryLevel2.getParentCategory();
        assertNotNull(categoryLevel1);
        assertEquals(1, categoryLevel1.getLevel());
        assertEquals("cat1", categoryLevel1.getCategoryId());

        verify(categoryRepository, times(3)).save(any(Category.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void calculateDiscountPercentage_shouldReturnCorrectDiscount() {
        int discount = ProductServiceImpl.calculateDiscountPercentage(200.0, 150.0);
        assertEquals(25, discount);
    }

    @Test
    void calculateDiscountPercentage_shouldThrowExceptionWhenMrpIsZeroOrLess() {
        assertThrows(IllegalArgumentException.class, () ->
                ProductServiceImpl.calculateDiscountPercentage(0, 100));
        assertThrows(IllegalArgumentException.class, () ->
                ProductServiceImpl.calculateDiscountPercentage(-10, 5));
    }

    @Test
    void deleteProduct_shouldDeleteExistingProduct() throws ProductException {
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> productService.deleteProduct(1L));
    }

    @Test
    void updateProduct_shouldSaveAndReturnUpdatedProduct() throws ProductException {
        Product productToUpdate = new Product();
        productToUpdate.setId(1L);
        productToUpdate.setTitle("Old Title");

        when(productRepository.findById(1L)).thenReturn(Optional.of(productToUpdate));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simulate update
        productToUpdate.setTitle("New Title");

        Product updated = productService.updateProduct(1L, productToUpdate);

        assertEquals(1L, updated.getId());
        assertEquals("New Title", updated.getTitle());
        verify(productRepository).save(productToUpdate);
    }

    @Test
    void updateProductStock_shouldToggleStockAndSave() throws ProductException {
        Product product = new Product();
        product.setId(1L);
        product.setIn_stock(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProductStock(1L);

        assertFalse(updated.isIn_stock());
        verify(productRepository).save(product);
    }

    @Test
    void findProductById_shouldReturnProductWhenFound() throws ProductException {
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product found = productService.findProductById(1L);
        assertEquals(product, found);
    }

    @Test
    void findProductById_shouldThrowExceptionWhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> productService.findProductById(1L));
    }

    @Test
    void searchProduct_shouldReturnListFromRepository() {
        List<Product> products = List.of(new Product(), new Product());

        when(productRepository.searchProduct("query")).thenReturn(products);

        List<Product> result = productService.searchProduct("query");
        assertEquals(products, result);
    }

    @Test
    void getAllProduct_shouldReturnPagedResult() {
        Page<Product> page = new PageImpl<>(List.of(new Product(), new Product()));

        // Explicit cast to JpaSpecificationExecutor to avoid ambiguous method reference
        when(((JpaSpecificationExecutor<Product>) productRepository)
                .findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        Page<Product> result = productService.getAllProduct("cat1", null, "red", "M",
                10, 100, 5, "price_low", "instock", 0);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(((JpaSpecificationExecutor<Product>) productRepository))
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void recentlyAddedProduct_shouldReturnEmptyList() {
        List<Product> products = productService.recentlyAddedProduct();
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    void getProductBySellerId_shouldReturnProductsFromRepository() {
        List<Product> products = List.of(new Product(), new Product());

        when(productRepository.findBySellerId(1L)).thenReturn(products);

        List<Product> result = productService.getProductBySellerId(1L);
        assertEquals(products, result);
    }
}
