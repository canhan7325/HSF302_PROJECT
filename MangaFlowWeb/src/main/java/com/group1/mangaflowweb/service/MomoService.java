package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.momo.MomoPaymentResponse;

public interface MomoService {
    MomoPaymentResponse createPayment(String orderId, String amount, String orderInfo);
}
