package com.ramyastore.service;

import com.ramyastore.model.Seller;
import com.ramyastore.model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport( SellerReport sellerReport);

}
