package com.ramyastore.controller;

import com.ramyastore.model.Cart;
import com.ramyastore.model.Coupon;
import com.ramyastore.model.User;
import com.ramyastore.service.CartService;
import com.ramyastore.service.CouponService;
import com.ramyastore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminCouponControllerTest {

    @InjectMocks
    private AdminCouponController adminCouponController;

    @Mock
    private CouponService couponService;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void applyCoupon_WhenApplyIsTrue_ShouldReturnCartWithAppliedCoupon() throws Exception {
        String jwt = "mock-jwt-token";
        String code = "DISCOUNT10";
        double orderValue = 100.0;

        User mockUser = new User();
        Cart mockCart = new Cart();

        when(userService.findUserProfileByJwt(jwt)).thenReturn(mockUser);
        when(couponService.applyCoupon(code, orderValue, mockUser)).thenReturn(mockCart);

        ResponseEntity<Cart> response = adminCouponController.applyCoupon("true", code, orderValue, jwt);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockCart, response.getBody());

        verify(userService, times(1)).findUserProfileByJwt(jwt);
        verify(couponService, times(1)).applyCoupon(code, orderValue, mockUser);
    }

    @Test
    void applyCoupon_WhenApplyIsFalse_ShouldReturnCartWithCouponRemoved() throws Exception {
        String jwt = "mock-jwt-token";
        String code = "DISCOUNT10";
        double orderValue = 100.0;

        User mockUser = new User();
        Cart mockCart = new Cart();

        when(userService.findUserProfileByJwt(jwt)).thenReturn(mockUser);
        when(couponService.removeCoupon(code, mockUser)).thenReturn(mockCart);

        ResponseEntity<Cart> response = adminCouponController.applyCoupon("false", code, orderValue, jwt);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockCart, response.getBody());

        verify(userService, times(1)).findUserProfileByJwt(jwt);
        verify(couponService, times(1)).removeCoupon(code, mockUser);
    }

    @Test
    void createCoupon_ShouldReturnCreatedCoupon() {
        Coupon inputCoupon = new Coupon();
        inputCoupon.setCode("SAVE20");

        Coupon createdCoupon = new Coupon();
        createdCoupon.setId(1L);
        createdCoupon.setCode("SAVE20");

        when(couponService.createCoupon(inputCoupon)).thenReturn(createdCoupon);

        ResponseEntity<Coupon> response = adminCouponController.createCoupon(inputCoupon);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(createdCoupon, response.getBody());

        verify(couponService, times(1)).createCoupon(inputCoupon);
    }

    @Test
    void deleteCoupon_ShouldReturnSuccessMessage() {
        Long couponId = 1L;

        doNothing().when(couponService).deleteCoupon(couponId);

        ResponseEntity<?> response = adminCouponController.deleteCoupon(couponId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Coupon deleted successfully", response.getBody());

        verify(couponService, times(1)).deleteCoupon(couponId);
    }

    @Test
    void getAllCoupons_ShouldReturnListOfCoupons() {
        Coupon c1 = new Coupon();
        c1.setId(1L);
        c1.setCode("OFF10");

        Coupon c2 = new Coupon();
        c2.setId(2L);
        c2.setCode("OFF20");

        List<Coupon> couponList = Arrays.asList(c1, c2);

        when(couponService.getAllCoupons()).thenReturn(couponList);

        ResponseEntity<List<Coupon>> response = adminCouponController.getAllCoupons();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(couponList, response.getBody());

        verify(couponService, times(1)).getAllCoupons();
    }
}
