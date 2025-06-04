package com.ramyastore.ai.services;

import com.ramyastore.exception.ProductException;
import com.ramyastore.mapper.OrderMapper;
import com.ramyastore.mapper.ProductMapper;
import com.ramyastore.model.*;
import com.ramyastore.repository.*;
import com.ramyastore.response.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatBotServiceImpl implements AiChatBotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private JSONArray createFunctionDeclarations() {
        return new JSONArray()
                .put(new JSONObject()
                        .put("name", "getUserCart")
                        .put("description", "Retrieve the user's cart details")
                        .put("parameters", new JSONObject()
                                .put("type", "OBJECT")
                                .put("properties", new JSONObject()
                                        .put("cart", new JSONObject()
                                                .put("type", "STRING")
                                                .put("description", "Cart details")
                                        )
                                )
                                .put("required", new JSONArray().put("cart"))
                        )
                )
                .put(new JSONObject()
                        .put("name", "getUsersOrder")
                        .put("description", "Retrieve user's order details")
                        .put("parameters", new JSONObject()
                                .put("type", "OBJECT")
                                .put("properties", new JSONObject()
                                        .put("order", new JSONObject()
                                                .put("type", "STRING")
                                                .put("description", "Order details")
                                        )
                                )
                                .put("required", new JSONArray().put("order"))
                        )
                )
                .put(new JSONObject()
                        .put("name", "getProductDetails")
                        .put("description", "Retrieve product details")
                        .put("parameters", new JSONObject()
                                .put("type", "OBJECT")
                                .put("properties", new JSONObject()
                                        .put("product", new JSONObject()
                                                .put("type", "STRING")
                                                .put("description", "Product details")
                                        )
                                )
                                .put("required", new JSONArray().put("product"))
                        )
                );
    }

    private FunctionResponse processFunctionCall(JSONObject functionCall, Long productId, Long userId) throws ProductException {
        String functionName = functionCall.getString("name");
        JSONObject args = functionCall.getJSONObject("args");

        FunctionResponse res = new FunctionResponse();
        res.setFunctionName(functionName);
        User user = userRepository.findById(userId).orElse(null);

        switch (functionName) {
            case "getUserCart":
                Cart cart = cartRepository.findByUserId(userId);
                res.setUserCart(cart);
                break;
            case "getUsersOrder":
                List<Order> orders = orderRepository.findByUserId(userId);
                res.setOrderHistory(OrderMapper.toOrderHistory(orders, user));
                break;
            case "getProductDetails":
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductException("product not found"));
                res.setProduct(product);
                break;
            default:
                throw new IllegalArgumentException("Unsupported function: " + functionName);
        }

        return res;
    }

    public FunctionResponse getFunctionResponse(String prompt, Long productId, Long userId) throws ProductException {
        String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + geminiApiKey;

        JSONObject requestBodyJson = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt))
                                )
                        )
                ).put("tools", new JSONArray()
                        .put(new JSONObject()
                                .put("functionDeclarations", createFunctionDeclarations())
                        )
                );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, requestEntity, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONObject functionCall = jsonObject
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getJSONObject("functionCall");

        return processFunctionCall(functionCall, productId, userId);
    }

    @Override
    public ApiResponse aiChatBot(String prompt, Long productId, Long userId) throws ProductException {
        String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + geminiApiKey;

        FunctionResponse functionResponse = getFunctionResponse(prompt, productId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("parts", new JSONArray().put(new JSONObject().put("text", prompt))))
                        .put(new JSONObject()
                                .put("role", "model")
                                .put("parts", new JSONArray().put(new JSONObject()
                                        .put("functionCall", new JSONObject()
                                                .put("name", functionResponse.getFunctionName())
                                                .put("args", new JSONObject()
                                                        .put("cart", functionResponse.getUserCart() != null ? functionResponse.getUserCart().getUser() : null)
                                                        .put("order", functionResponse.getOrderHistory() != null ? functionResponse.getOrderHistory() : null)
                                                        .put("product", functionResponse.getProduct() != null ? ProductMapper.toProductDto(functionResponse.getProduct()) : null)
                                                )
                                        )
                                )))
                        .put(new JSONObject()
                                .put("role", "function")
                                .put("parts", new JSONArray().put(new JSONObject()
                                        .put("functionResponse", new JSONObject()
                                                .put("name", functionResponse.getFunctionName())
                                                .put("response", new JSONObject()
                                                        .put("name", functionResponse.getFunctionName())
                                                        .put("content", functionResponse)
                                                )
                                        )
                                )))
                )
                .put("tools", new JSONArray()
                        .put(new JSONObject()
                                .put("functionDeclarations", createFunctionDeclarations()))
                );

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, request, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());
        String message = jsonObject
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        ApiResponse res = new ApiResponse();
        res.setMessage(message);
        return res;
    }
}
