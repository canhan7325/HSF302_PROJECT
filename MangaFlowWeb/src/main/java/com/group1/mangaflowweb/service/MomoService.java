package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.payment.MomoPaymentDTO;

public interface MomoService {
    MomoPaymentDTO createPayment(String orderId, String amount, String orderInfo);
}

