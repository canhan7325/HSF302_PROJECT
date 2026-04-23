package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.payment.ZaloPayPaymentDTO;

public interface ZaloPayService {
    ZaloPayPaymentDTO createPayment(String orderId, String amount, String orderInfo);
}

