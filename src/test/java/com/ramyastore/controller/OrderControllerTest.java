package com.ramyastore.controller;

import com.razorpay.PaymentLink;
import com.ramyastore.domain.PaymentMethod;
import com.ramyastore.exception.OrderException;
import com.ramyastore.exception.SellerException;
import com.ramyastore.exception.UserException;
import com.ramyastore.model.*;
import com.ramyastore.repository.PaymentOrderRepository;
import com.ramyastore.response.PaymentLinkResponse;
import com.ramyastore.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private CartService cartService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private SellerReportService sellerReportService;

    @Mock
    private SellerService sellerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrderHandler_WithRazorpayPayment_Success() throws Exception {
        String jwt = "jwt-token";
        Address address = new Address();

        User user = new User();
        Cart cart = new Cart();
        Set<Order> orders = new HashSet<>();
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setId(1L);
        paymentOrder.setAmount(1000L);

        PaymentLink paymentLink = mock(PaymentLink.class);
        when(paymentLink.get("short_url")).thenReturn("http://razorpay.link");
        when(paymentLink.get("id")).thenReturn("razorpay-payment-id");

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(cartService.findUserCart(user)).thenReturn(cart);
        when(orderService.createOrder(user, address, cart)).thenReturn(orders);
        when(paymentService.createOrder(user, orders)).thenReturn(paymentOrder);
        when(paymentService.createRazorpayPaymentLink(user, paymentOrder.getAmount(), paymentOrder.getId()))
                .thenReturn(paymentLink);

        ResponseEntity<PaymentLinkResponse> response = orderController.createOrderHandler(address, PaymentMethod.RAZORPAY, jwt);

        assertEquals(200, response.getStatusCodeValue());
        PaymentLinkResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("http://razorpay.link", body.getPayment_link_url());

        verify(paymentOrderRepository).save(paymentOrder);
        assertEquals("razorpay-payment-id", paymentOrder.getPaymentLinkId());
    }

    @Test
    void createOrderHandler_WithStripePayment_Success() throws Exception {
        String jwt = "jwt-token";
        Address address = new Address();

        User user = new User();
        Cart cart = new Cart();
        Set<Order> orders = new HashSet<>();
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setId(1L);
        paymentOrder.setAmount(500L);

        String stripePaymentUrl = "http://stripe.payment.link";

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(cartService.findUserCart(user)).thenReturn(cart);
        when(orderService.createOrder(user, address, cart)).thenReturn(orders);
        when(paymentService.createOrder(user, orders)).thenReturn(paymentOrder);
        when(paymentService.createStripePaymentLink(user, paymentOrder.getAmount(), paymentOrder.getId()))
                .thenReturn(stripePaymentUrl);

        ResponseEntity<PaymentLinkResponse> response = orderController.createOrderHandler(address, PaymentMethod.STRIPE, jwt);

        assertEquals(200, response.getStatusCodeValue());
        PaymentLinkResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(stripePaymentUrl, body.getPayment_link_url());

        verify(paymentOrderRepository, never()).save(any()); // No save call for stripe flow
    }

    @Test
    void usersOrderHistoryHandler_Success() throws UserException {
        String jwt = "jwt-token";
        User user = new User();
        List<Order> orders = new ArrayList<>();

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(orderService.usersOrderHistory(user.getId())).thenReturn(orders);

        ResponseEntity<List<Order>> response = orderController.usersOrderHistoryHandler(jwt);

        assertEquals(202, response.getStatusCodeValue());
        assertEquals(orders, response.getBody());
    }

    @Test
    void getOrderById_Success() throws OrderException, UserException {
        String jwt = "jwt-token";
        Long orderId = 10L;

        User user = new User();
        Order order = new Order();

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(orderService.findOrderById(orderId)).thenReturn(order);

        ResponseEntity<Order> response = orderController.getOrderById(orderId, jwt);

        assertEquals(202, response.getStatusCodeValue());
        assertEquals(order, response.getBody());
    }

    @Test
    void getOrderItemById_Success() throws Exception {
        String jwt = "jwt-token";
        Long orderItemId = 5L;

        User user = new User();
        OrderItem orderItem = new OrderItem();

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(orderItemService.getOrderItemById(orderItemId)).thenReturn(orderItem);

        ResponseEntity<OrderItem> response = orderController.getOrderItemById(orderItemId, jwt);

        assertEquals(202, response.getStatusCodeValue());
        assertEquals(orderItem, response.getBody());
    }

    @Test
    void cancelOrder_Success() throws UserException, OrderException, SellerException {
        String jwt = "jwt-token";
        Long orderId = 20L;

        User user = new User();
        Order order = new Order();
        order.setSellerId(1L);

        Seller seller = new Seller();
        SellerReport sellerReport = new SellerReport();
        sellerReport.setCanceledOrders(0);
        sellerReport.setTotalRefunds(0L);

        when(userService.findUserProfileByJwt(jwt)).thenReturn(user);
        when(orderService.cancelOrder(orderId, user)).thenReturn(order);
        when(sellerService.getSellerById(order.getSellerId())).thenReturn(seller);
        when(sellerReportService.getSellerReport(seller)).thenReturn(sellerReport);

        ResponseEntity<Order> response = orderController.cancelOrder(orderId, jwt);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(order, response.getBody());
        assertEquals(1, sellerReport.getCanceledOrders());
        assertEquals(order.getTotalSellingPrice(), sellerReport.getTotalRefunds());

        verify(sellerReportService).updateSellerReport(sellerReport);
    }
}
