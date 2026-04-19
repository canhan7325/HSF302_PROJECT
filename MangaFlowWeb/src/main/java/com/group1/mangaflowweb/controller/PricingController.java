package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.SubscriptionsDTO;
import com.group1.mangaflowweb.dto.momo.MomoPaymentResponse;
import com.group1.mangaflowweb.dto.zalopay.ZaloPayPaymentResponse;
import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.service.MomoService;
import com.group1.mangaflowweb.service.SubcriptionsService;
import com.group1.mangaflowweb.service.TransactionsService;
import com.group1.mangaflowweb.service.ZaloPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
@RequestMapping("/pricing")
public class PricingController {

    @Autowired
    private SubcriptionsService subscriptionsService;

    @Autowired
    private MomoService momoService;

    @Autowired
    private ZaloPayService zaloPayService;

    @Autowired
    private TransactionsService transactionsService;

    @GetMapping
    public String getPricingPage(Model model) {
        model.addAttribute("subscriptions", subscriptionsService.getAllActiveSubscriptions());
        return "clients/buysubscriptions";
    }

    @PostMapping("/pay")
    public RedirectView paySubscription(
            @RequestParam("subscriptionId") Integer subscriptionId,
            @RequestParam(value = "payment", defaultValue = "momo") String payment) {

        SubscriptionsDTO subscription = subscriptionsService.getSubscriptionById(subscriptionId);
        if (subscription == null) {
            return new RedirectView("/pricing?error=invalid_subscription");
        }

        String amount = String.valueOf(subscription.getPrice().longValue());
        // Use subscriptionId as part of orderId so we can reference it in callback
        String orderId = subscriptionId + "_" + UUID.randomUUID().toString().substring(0, 8);
        String orderInfo = "Thanh toan goi " + subscription.getName();

        try {
            if ("zalopay".equalsIgnoreCase(payment)) {
                ZaloPayPaymentResponse response = zaloPayService.createPayment(orderId, amount, orderInfo);
                if (response != null && response.getOrder_url() != null && !response.getOrder_url().isEmpty()) {
                    return new RedirectView(response.getOrder_url());
                }
            } else {
                MomoPaymentResponse response = momoService.createPayment(orderId, amount, orderInfo);
                if (response != null && response.getPayUrl() != null && !response.getPayUrl().isEmpty()) {
                    return new RedirectView(response.getPayUrl());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new RedirectView("/pricing?error=payment_failed");
    }
}
