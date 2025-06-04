package com.ramyastore.response;

import com.ramyastore.dto.OrderHistory;
import com.ramyastore.model.Cart;
import com.ramyastore.model.Product;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponse {
    private String functionName;
    private Cart userCart;
    private OrderHistory orderHistory;
    private Product product;
}
