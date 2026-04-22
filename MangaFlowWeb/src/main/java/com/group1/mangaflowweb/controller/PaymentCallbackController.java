package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.TransactionsDTO;
import com.group1.mangaflowweb.service.TransactionService;
import com.group1.mangaflowweb.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/payment")
public class PaymentCallbackController {

    private final TransactionService transactionsService;
    private final UserService userService;

    public PaymentCallbackController(TransactionService transactionsService, UserService userService) {
        this.transactionsService = transactionsService;
        this.userService = userService;
    }

    /**
     * ZaloPay callback - user is redirected here after payment
     */
    @GetMapping("/zalopay/return")
    public Object zaloPayReturn(
            @RequestParam(value = "status", required = false, defaultValue = "0") String status,
            @RequestParam(value = "apptransid", required = false, defaultValue = "") String appTransId,
            @RequestParam(value = "amount", required = false, defaultValue = "0") String amount,
            HttpSession session,
            Model model) {

        // DEBUG: Log all parameters
        System.out.println("=== ZaloPay Callback Debug ===");
        System.out.println("Status: " + status);
        System.out.println("AppTransId: " + appTransId);
        System.out.println("Amount: " + amount);

        // Check if already processed
        String processedTransId = (String) session.getAttribute("processed_" + appTransId);
        if (processedTransId != null) {
            System.out.println("Transaction already processed!");
            return handleSuccessDisplay(model, session, appTransId);
        }

        if ("1".equals(status)) {
            try {
                Integer subscriptionId = parseSubscriptionId(appTransId);
                Integer userId = getCurrentUserId();

                System.out.println("Parsed SubscriptionId: " + subscriptionId);
                System.out.println("Current UserId: " + userId);

                // Create transaction with current user ID
                TransactionsDTO dto = transactionsService.createTransaction(userId, subscriptionId, new BigDecimal(amount));

                // Get discount info from session
                Long discountAmount = (Long) session.getAttribute("discountAmount");
                Boolean isUpgrade = (Boolean) session.getAttribute("isUpgrade");

                if (discountAmount == null) discountAmount = 0L;
                if (isUpgrade == null) isUpgrade = false;

                System.out.println("Discount: " + discountAmount + ", IsUpgrade: " + isUpgrade);

                TransactionsDTO completed = transactionsService.completeTransaction(
                    dto.getTransactionId(),
                    discountAmount,
                    isUpgrade
                );

                // Clear discount info from session after use
                session.removeAttribute("discountAmount");
                session.removeAttribute("isUpgrade");

                if (completed != null) {
                    // Mark as processed in session and save all data
                    session.setAttribute("processed_" + appTransId, "true");
                    saveTransactionToSession(session, appTransId, completed, amount);

                    System.out.println("Transaction completed successfully!");
                    populateSuccessModel(model, completed, amount);
                    return "clients/payment-success";
                }
            } catch (Exception e) {
                System.out.println("Error processing transaction: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("errorMessage", "Lỗi xử lý thanh toán: " + e.getMessage());
            }
        } else {
            System.out.println("Status is not '1', payment failed or cancelled");
        }

        model.addAttribute("errorMessage", "Giao dịch ZaloPay bị hủy hoặc không thành công.");
        return "clients/payment-failed";
    }

    /**
     * MoMo callback - user is redirected here after payment
     */
    @GetMapping("/momo/return")
    public Object momoReturn(
            @RequestParam(value = "resultCode", required = false, defaultValue = "-1") String resultCode,
            @RequestParam(value = "orderId", required = false, defaultValue = "") String orderId,
            @RequestParam(value = "amount", required = false, defaultValue = "0") String amount,
            HttpSession session,
            Model model) {

        // Check if already processed
        String processedOrderId = (String) session.getAttribute("processed_" + orderId);
        if (processedOrderId != null) {
            return handleSuccessDisplay(model, session, orderId);
        }

        if ("0".equals(resultCode)) {
            try {
                Integer subscriptionId = parseSubscriptionId(orderId);
                Integer userId = getCurrentUserId();

                TransactionsDTO dto = transactionsService.createTransaction(userId, subscriptionId, new BigDecimal(amount));

                // Get discount info from session
                Long discountAmount = (Long) session.getAttribute("discountAmount");
                Boolean isUpgrade = (Boolean) session.getAttribute("isUpgrade");

                if (discountAmount == null) discountAmount = 0L;
                if (isUpgrade == null) isUpgrade = false;

                TransactionsDTO completed = transactionsService.completeTransaction(
                    dto.getTransactionId(),
                    discountAmount,
                    isUpgrade
                );

                // Clear discount info from session after use
                session.removeAttribute("discountAmount");
                session.removeAttribute("isUpgrade");

                if (completed != null) {
                    // Mark as processed in session and save all data
                    session.setAttribute("processed_" + orderId, "true");
                    saveTransactionToSession(session, orderId, completed, amount);

                    populateSuccessModel(model, completed, amount);
                    return "clients/payment-success";
                }
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Lỗi xử lý thanh toán: " + e.getMessage());
            }
        }

        model.addAttribute("errorMessage", "Giao dịch MoMo bị hủy hoặc không thành công. Mã lỗi: " + resultCode);
        return "clients/payment-failed";
    }

    /**
     * Save transaction data to session for F5 refresh
     */
    private void saveTransactionToSession(HttpSession session, String transId, TransactionsDTO dto, String amount) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        session.setAttribute("transaction_" + transId + "_id", dto.getTransactionId());
        session.setAttribute("transaction_" + transId + "_name", dto.getSubscriptionName());
        session.setAttribute("transaction_" + transId + "_amount", amount);
        session.setAttribute("transaction_" + transId + "_started", dto.getStartedAt() != null ? dto.getStartedAt().format(fmt) : "");
        session.setAttribute("transaction_" + transId + "_ended", dto.getEndedAt() != null ? dto.getEndedAt().format(fmt) : "");
        
        // Save membership for header display
        String membership = transactionsService.getMembershipFromPrice(dto.getPrice());
        session.setAttribute("transaction_" + transId + "_membership", membership);
    }

    /**
     * Handle success display from session
     */
    private String handleSuccessDisplay(Model model, HttpSession session, String transId) {
        Integer transactionId = (Integer) session.getAttribute("transaction_" + transId + "_id");
        if (transactionId != null) {
            // Display success page with cached data from session
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("subscriptionName", session.getAttribute("transaction_" + transId + "_name"));
            model.addAttribute("amount", session.getAttribute("transaction_" + transId + "_amount"));
            model.addAttribute("startedAt", session.getAttribute("transaction_" + transId + "_started"));
            model.addAttribute("endedAt", session.getAttribute("transaction_" + transId + "_ended"));

            // Add membership for header display
            String membership = (String) session.getAttribute("transaction_" + transId + "_membership");
            if (membership != null) {
                model.addAttribute("userMembership", membership);
            }

            return "clients/payment-success";
        }
        model.addAttribute("errorMessage", "Không tìm thấy thông tin giao dịch.");
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

        // Add membership for header display
        String membership = transactionsService.getMembershipFromPrice(dto.getPrice());
        model.addAttribute("userMembership", membership);
    }

    /**
     * Parse subscriptionId from apptransid/orderId
     * Format from ZaloPay: yyMMdd_subscriptionId_uuid
     * Format from MoMo: subscriptionId_uuid
     */
    private Integer parseSubscriptionId(String transId) {
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
                    return id;
                } catch (NumberFormatException ignored) {
                    // ...ignored
                }
            }
        } catch (Exception e) {
            // ...error parsing
        }
        return 1;
    }

    /**
     * Get current logged-in user ID from authentication
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return 1; // fallback to user 1 if not authenticated
        }

        String username = authentication.getName();
        com.group1.mangaflowweb.dto.user.UserResponse user = userService.findByUsername(username);
        return user != null ? user.getUserId() : 1;
    }
}





