package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.zalopay.ZaloPayPaymentResponse;

public interface ZaloPayService {
    ZaloPayPaymentResponse createPayment(String orderId, String amount, String orderInfo);
}
