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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/payment")
public class PaymentCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCallbackController.class);

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

        logger.info("ZaloPay callback - status: {}, appTransId: {}, amount: {}", status, appTransId, amount);

        if ("1".equals(status)) {
            try {
                Integer subscriptionId = parseSubscriptionId(appTransId);
                logger.info("Parsed subscriptionId: {}", subscriptionId);

                // userId = 1 tạm thời, sẽ thay bằng user đã đăng nhập
                TransactionsDTO dto = transactionsService.createTransaction(1, subscriptionId, new BigDecimal(amount));
                logger.info("Created transaction: {}", dto.getTransactionId());

                TransactionsDTO completed = transactionsService.completeTransaction(dto.getTransactionId());
                logger.info("Completed transaction: {}", completed.getTransactionId());

                if (completed != null) {
                    populateSuccessModel(model, completed, amount);
                    return "clients/payment-success";
                }
            } catch (Exception e) {
                logger.error("Error processing ZaloPay callback", e);
                model.addAttribute("errorMessage", "Lỗi xử lý thanh toán: " + e.getMessage());
            }
        } else {
            logger.warn("ZaloPay payment failed with status: {}", status);
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

        logger.info("MoMo callback - resultCode: {}, orderId: {}, amount: {}", resultCode, orderId, amount);

        if ("0".equals(resultCode)) {
            try {
                Integer subscriptionId = parseSubscriptionId(orderId);
                logger.info("Parsed subscriptionId: {}", subscriptionId);

                TransactionsDTO dto = transactionsService.createTransaction(1, subscriptionId, new BigDecimal(amount));
                logger.info("Created transaction: {}", dto.getTransactionId());

                TransactionsDTO completed = transactionsService.completeTransaction(dto.getTransactionId());
                logger.info("Completed transaction: {}", completed.getTransactionId());

                if (completed != null) {
                    populateSuccessModel(model, completed, amount);
                    return "clients/payment-success";
                }
            } catch (Exception e) {
                logger.error("Error processing MoMo callback", e);
                model.addAttribute("errorMessage", "Lỗi xử lý thanh toán: " + e.getMessage());
            }
        } else {
            logger.warn("MoMo payment failed with resultCode: {}", resultCode);
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
     * Parse subscriptionId from apptransid/orderId
     * Format from ZaloPay: yyMMdd_subscriptionId_uuid
     * Format from MoMo: subscriptionId_uuid
     */
    private Integer parseSubscriptionId(String transId) {
        logger.info("Attempting to parse subscriptionId from transId: {}", transId);
        try {
            String[] parts = transId.split("_");
            // ZaloPay format: yyMMdd_subscriptionId_uuid (skip first part which is date)
            // MoMo format: subscriptionId_uuid
            for (int i = 0; i < parts.length; i++) {
                try {
                    Integer id = Integer.parseInt(parts[i]);
                    // For ZaloPay, first part is date (6 digits), skip it
                    // Second part should be subscriptionId
                    if (i == 0 && parts.length > 1 && parts[i].length() == 6) {
                        // This looks like a date (yyMMdd), skip to next
                        continue;
                    }
                    logger.info("Found subscriptionId: {} at index {}", id, i);
                    return id;
                } catch (NumberFormatException ignored) {
                    logger.debug("Part {} is not a number: {}", i, parts[i]);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing subscriptionId from transId: {}", transId, e);
        }
        logger.warn("Could not parse subscriptionId, defaulting to 1");
        return 1;
    }
}
