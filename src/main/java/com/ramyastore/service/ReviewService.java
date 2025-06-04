package com.ramyastore.service;

import com.ramyastore.exception.ReviewNotFoundException;
import com.ramyastore.model.Product;
import com.ramyastore.model.Review;
import com.ramyastore.model.User;
import com.ramyastore.request.CreateReviewRequest;

import javax.naming.AuthenticationException;
import java.util.List;

public interface ReviewService {

    Review createReview(CreateReviewRequest req,
                        User user,
                        Product product);

    List<Review> getReviewsByProductId(Long productId);

    Review updateReview(Long reviewId,
                        String reviewText,
                        double rating,
                        Long userId) throws ReviewNotFoundException, AuthenticationException;


    void deleteReview(Long reviewId, Long userId) throws ReviewNotFoundException, AuthenticationException;

}
