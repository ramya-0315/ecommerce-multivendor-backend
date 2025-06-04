package com.ramyastore.ai.services;

import com.ramyastore.exception.ProductException;
import com.ramyastore.response.ApiResponse;

public interface AiChatBotService {

    ApiResponse aiChatBot(String prompt,Long productId,Long userId) throws ProductException;
}
