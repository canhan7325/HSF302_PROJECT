package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.TransactionsDTO;
import com.group1.mangaflowweb.service.SubcriptionsService;
import com.group1.mangaflowweb.service.TransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/payment")
public class PaymentCallbackController {

    @Autowired
    private TransactionsService transactionsService;

    @Autowired
    private SubcriptionsService subscriptionsService;

    /**
     * ZaloPay callback - user is redirected here after payment
     */
    @GetMapping("/zalopay/return")
    public String zaloPayReturn(
            @RequestParam(value = "status", required = false, defaultValue = "0") String status,
            @RequestParam(value = "apptransid", required = false, defaultValue = "") String appTransId,
            @RequestParam(value = "amount", required = false, defaultValue = "0") String amount,
            Model model) {

        if ("1".equals(status)) {
            try {
                Integer subscriptionId = parseSubscriptionId(appTransId);
                // userId = 1 tạm thời, sẽ thay bằng user đã đăng nhập
                TransactionsDTO dto = transactionsService.createTransaction(1, subscriptionId, new BigDecimal(amount));
                TransactionsDTO completed = transactionsService.completeTransaction(dto.getTransactionId());

                if (completed != null) {
                    populateSuccessModel(model, completed, amount);
                    return "clients/payment-success";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("errorMessage", "Giao dịch ZaloPay bị hủy hoặc không thành công.");
        return "clients/payment-failed";
    }

    /**
     * MoMo callback - user is redirected here after payment
     */
    @GetMapping("/momo/return")
    public String momoReturn(
            @RequestParam(value = "resultCode", required = false, defaultValue = "-1") String resultCode,
            @RequestParam(value = "orderId", required = false, defaultValue = "") String orderId,
            @RequestParam(value = "amount", required = false, defaultValue = "0") String amount,
            Model model) {

        if ("0".equals(resultCode)) {
            try {
                Integer subscriptionId = parseSubscriptionId(orderId);
                TransactionsDTO dto = transactionsService.createTransaction(1, subscriptionId, new BigDecimal(amount));
                TransactionsDTO completed = transactionsService.completeTransaction(dto.getTransactionId());

                if (completed != null) {
                    populateSuccessModel(model, completed, amount);
                    return "clients/payment-success";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("errorMessage", "Giao dịch MoMo bị hủy hoặc không thành công. Mã lỗi: " + resultCode);
        return "clients/payment-failed";
    }

    /**
     * Populate model for success page
     */
    private void populateSuccessModel(Model model, TransactionsDTO dto, String amount) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        model.addAttribute("transactionId", dto.getTransactionId());
        model.addAttribute("subscriptionName", dto.getSubscriptionName());
        model.addAttribute("amount", amount);
        model.addAttribute("startedAt", dto.getStartedAt() != null ? dto.getStartedAt().format(fmt) : "");
        model.addAttribute("endedAt", dto.getEndedAt() != null ? dto.getEndedAt().format(fmt) : "");
    }

    /**
     * Parse subscriptionId from orderId (format: subscriptionId_xxx)
     */
    private Integer parseSubscriptionId(String orderId) {
        try {
            String[] parts = orderId.split("_");
            for (String part : parts) {
                try {
                    return Integer.parseInt(part);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}
