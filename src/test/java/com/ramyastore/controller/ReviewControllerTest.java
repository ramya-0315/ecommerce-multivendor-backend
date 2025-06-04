package com.ramyastore.controller;

import com.ramyastore.exception.ProductException;
import com.ramyastore.exception.ReviewNotFoundException;
import com.ramyastore.exception.UserException;
import com.ramyastore.model.Product;
import com.ramyastore.model.Review;
import com.ramyastore.model.User;
import com.ramyastore.request.CreateReviewRequest;
import com.ramyastore.response.ApiResponse;
import com.ramyastore.service.ProductService;
import com.ramyastore.service.ReviewService;
import com.ramyastore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.naming.AuthenticationException;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper;

    private final String AUTH_HEADER = "Bearer test.jwt.token";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getReviewsByProductId_returnsList() throws Exception {
        Review review = new Review();
        List<Review> reviews = Collections.singletonList(review);

        when(reviewService.getReviewsByProductId(1L)).thenReturn(reviews);

        mockMvc.perform(get("/api/products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(reviewService, times(1)).getReviewsByProductId(1L);
    }

    @Test
    void writeReview_createsAndReturnsReview() throws Exception {
        CreateReviewRequest req = new CreateReviewRequest();
        req.setReviewRating(5);
        req.setReviewText("Great product!");

        User user = new User();
        Product product = new Product();
        Review review = new Review();

        when(userService.findUserProfileByJwt(anyString())).thenReturn(user);
        when(productService.findProductById(anyLong())).thenReturn(product);
        when(reviewService.createReview(any(CreateReviewRequest.class), eq(user), eq(product))).thenReturn(review);

        mockMvc.perform(post("/api/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(userService, times(1)).findUserProfileByJwt(AUTH_HEADER);
        verify(productService, times(1)).findProductById(1L);
        verify(reviewService, times(1)).createReview(any(), eq(user), eq(product));
    }

    @Test
    void updateReview_updatesAndReturnsReview() throws Exception {
        CreateReviewRequest req = new CreateReviewRequest();
        req.setReviewText("Updated review");
        req.setReviewRating(4);

        User user = new User();
        user.setId(10L);
        Review review = new Review();

        when(userService.findUserProfileByJwt(anyString())).thenReturn(user);
        when(reviewService.updateReview(eq(5L), eq("Updated review"), eq(4), eq(10L))).thenReturn(review);

        mockMvc.perform(patch("/api/reviews/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_HEADER)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(userService, times(1)).findUserProfileByJwt(AUTH_HEADER);
        verify(reviewService, times(1)).updateReview(5L, "Updated review", 4, 10L);
    }

    @Test
    void deleteReview_deletesAndReturnsApiResponse() throws Exception {
        User user = new User();
        user.setId(15L);

        when(userService.findUserProfileByJwt(anyString())).thenReturn(user);
        doNothing().when(reviewService).deleteReview(7L, 15L);

        mockMvc.perform(delete("/api/reviews/7")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));

        verify(userService, times(1)).findUserProfileByJwt(AUTH_HEADER);
        verify(reviewService, times(1)).deleteReview(7L, 15L);
    }
}
