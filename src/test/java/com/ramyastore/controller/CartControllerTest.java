package com.ramyastore.controller;

import com.ramyastore.exception.CartItemException;
import com.ramyastore.exception.ProductException;
import com.ramyastore.exception.UserException;
import com.ramyastore.model.*;
import com.ramyastore.request.AddItemRequest;
import com.ramyastore.response.ApiResponse;
import com.ramyastore.service.CartItemService;
import com.ramyastore.service.CartService;
import com.ramyastore.service.ProductService;
import com.ramyastore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemService cartItemService;

    private User mockUser;
    private Cart mockCart;
    private CartItem mockCartItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");

        mockCart = new Cart();
        mockCart.setUser(mockUser);

        mockCartItem = new CartItem();
        mockCartItem.setId(100L);
        mockCartItem.setQuantity(2);
    }

    @Test
    void testFindUserCartHandler() throws UserException {
        String jwt = "mock-jwt";

        when(userService.findUserProfileByJwt(jwt)).thenReturn(mockUser);
        when(cartService.findUserCart(mockUser)).thenReturn(mockCart);

        ResponseEntity<Cart> response = cartController.findUserCartHandler(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCart, response.getBody());
    }

    @Test
    void testAddItemToCart() throws UserException, ProductException {
        String jwt = "mock-jwt";
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setSize("M");
        request.setQuantity(2);

        Product product = new Product();
        product.setId(1L);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(mockUser);
        when(productService.findProductById(1L)).thenReturn(product);
        when(cartService.addCartItem(mockUser, product, "M", 2)).thenReturn(mockCartItem);

        ResponseEntity<CartItem> response = cartController.addItemToCart(request, jwt);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(mockCartItem, response.getBody());
    }

    @Test
    void testDeleteCartItemHandler() throws CartItemException, UserException {
        String jwt = "mock-jwt";
        Long cartItemId = 100L;

        when(userService.findUserProfileByJwt(jwt)).thenReturn(mockUser);
        doNothing().when(cartItemService).removeCartItem(mockUser.getId(), cartItemId);

        ResponseEntity<ApiResponse> response = cartController.deleteCartItemHandler(cartItemId, jwt);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Item Remove From Cart", response.getBody().getMessage());
        assertTrue(response.getBody().isStatus());

    }

    @Test
    void testUpdateCartItemHandler() throws CartItemException, UserException {
        String jwt = "mock-jwt";
        Long cartItemId = 100L;

        CartItem inputCartItem = new CartItem();
        inputCartItem.setQuantity(3);

        CartItem updatedItem = new CartItem();
        updatedItem.setQuantity(3);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(mockUser);
        when(cartItemService.updateCartItem(mockUser.getId(), cartItemId, inputCartItem)).thenReturn(updatedItem);

        ResponseEntity<CartItem> response = cartController.updateCartItemHandler(cartItemId, inputCartItem, jwt);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(3, response.getBody().getQuantity());
    }

}
