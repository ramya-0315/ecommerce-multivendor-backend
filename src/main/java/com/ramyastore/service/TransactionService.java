package com.ramyastore.service;

import com.ramyastore.model.Order;
import com.ramyastore.model.Seller;
import com.ramyastore.model.Transaction;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Order order);
    List<Transaction> getTransactionBySeller(Seller seller);
    List<Transaction>getAllTransactions();
}
