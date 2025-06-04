package com.ramyastore.controller;

import com.ramyastore.domain.PaymentMethod;
import com.ramyastore.model.*;
import com.ramyastore.repository.CartItemRepository;
import com.ramyastore.repository.CartRepository;
import com.ramyastore.response.ApiResponse;
import com.ramyastore.response.PaymentLinkResponse;
import com.ramyastore.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private UserService userService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SellerReportService sellerReportService;

    @Mock
    private SellerService sellerService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void paymentHandler_ReturnsCreatedWithNullBody() throws Exception {
        String jwt = "jwt-token";
        Long orderId = 1L;
        PaymentMethod paymentMethod = PaymentMethod.RAZORPAY;

        User user = new User();
        PaymentOrder paymentOrder = new PaymentOrder();

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(paymentService.getPaymentOrderById(orderId)).thenReturn(paymentOrder);

        ResponseEntity<PaymentLinkResponse> response = paymentController.paymentHandler(paymentMethod, orderId, jwt);

        assertEquals(201, response.getStatusCodeValue());
        assertNull(response.getBody());  // As per code, body is null
    }

    @Test
    void paymentSuccessHandler_WithSuccessfulPayment_UpdatesReportsAndCart() throws Exception {
        String jwt = "jwt-token";
        String paymentId = "payment-id";
        String paymentLinkId = "payment-link-id";

        User user = new User();
        user.setId(100L);

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setOrders(new HashSet<>());

        Order order1 = new Order();
        order1.setSellerId(10L);
        order1.setTotalSellingPrice(500);  // use Integer, not Long
        order1.setOrderItems(Arrays.asList(new OrderItem(), new OrderItem())); // List, not Set

        Order order2 = new Order();
        order2.setSellerId(20L);
        order2.setTotalSellingPrice(1000);  // use Integer, not Long
        order2.setOrderItems(Collections.singletonList(new OrderItem()));  // List, not Set


        paymentOrder.getOrders().add(order1);
        paymentOrder.getOrders().add(order2);

        Seller seller1 = new Seller();
        Seller seller2 = new Seller();

        SellerReport report1 = new SellerReport();
        report1.setTotalOrders(0);
        report1.setTotalEarnings(0L);
        report1.setTotalSales(0L);

        SellerReport report2 = new SellerReport();
        report2.setTotalOrders(5);
        report2.setTotalEarnings(2000L);
        report2.setTotalSales(3L);

        Cart cart = new Cart();
        cart.setCouponCode("SAVE10");
        cart.setCouponPrice(50);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(paymentService.getPaymentOrderByPaymentId(paymentLinkId)).thenReturn(paymentOrder);
        when(paymentService.ProceedPaymentOrder(paymentOrder, paymentId, paymentLinkId)).thenReturn(true);

        when(sellerService.getSellerById(10L)).thenReturn(seller1);
        when(sellerService.getSellerById(20L)).thenReturn(seller2);

        when(sellerReportService.getSellerReport(seller1)).thenReturn(report1);
        when(sellerReportService.getSellerReport(seller2)).thenReturn(report2);

        when(cartRepository.findByUserId(user.getId())).thenReturn(cart);

        ResponseEntity<ApiResponse> response = paymentController.paymentSuccessHandler(paymentId, paymentLinkId, jwt);

        assertEquals(201, response.getStatusCodeValue());
        ApiResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isStatus());
        assertEquals("Payment successful", body.getMessage());

        // Verify transactions created for each order
        verify(transactionService).createTransaction(order1);
        verify(transactionService).createTransaction(order2);

        // Verify seller reports updated correctly
        assertEquals(1, report1.getTotalOrders());
        assertEquals(500L, report1.getTotalEarnings());
        assertEquals(2, report1.getTotalSales());

        assertEquals(6, report2.getTotalOrders());
        assertEquals(3000L, report2.getTotalEarnings());
        assertEquals(4, report2.getTotalSales());

        verify(sellerReportService).updateSellerReport(report1);
        verify(sellerReportService).updateSellerReport(report2);

        // Verify cart updated and saved
        assertEquals(0, cart.getCouponPrice());
        assertNull(cart.getCouponCode());
        verify(cartRepository).save(cart);
    }

    @Test
    void paymentSuccessHandler_WithFailedPayment_ReturnsSuccessResponseWithoutUpdates() throws Exception {
        String jwt = "jwt-token";
        String paymentId = "payment-id";
        String paymentLinkId = "payment-link-id";

        User user = new User();
        user.setId(100L);

        PaymentOrder paymentOrder = new PaymentOrder();

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(paymentService.getPaymentOrderByPaymentId(paymentLinkId)).thenReturn(paymentOrder);
        when(paymentService.ProceedPaymentOrder(paymentOrder, paymentId, paymentLinkId)).thenReturn(false);

        ResponseEntity<ApiResponse> response = paymentController.paymentSuccessHandler(paymentId, paymentLinkId, jwt);

        assertEquals(201, response.getStatusCodeValue());
        ApiResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isStatus());
        assertEquals("Payment successful", body.getMessage());

        // No updates should happen on failure
        verify(transactionService, never()).createTransaction(any());
        verify(sellerReportService, never()).updateSellerReport(any());
        verify(cartRepository, never()).save(any());
    }
}
