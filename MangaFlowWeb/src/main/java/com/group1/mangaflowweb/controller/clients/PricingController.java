package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.subscription.SubscriptionCheckDTO;
import com.group1.mangaflowweb.dto.subscription.SubscriptionsDTO;
import com.group1.mangaflowweb.dto.payment.MomoPaymentDTO;
import com.group1.mangaflowweb.dto.payment.ZaloPayPaymentDTO;
import com.group1.mangaflowweb.dto.user.UserDTO;
import com.group1.mangaflowweb.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private UserService userService;

    @GetMapping
    public String getPricingPage(Model model) {
        try {
            java.util.List<SubscriptionsDTO> subscriptions = subscriptionsService.getAllActiveSubscriptions();
            model.addAttribute("subscriptions", subscriptions);

            // Pre-calculate subscription checks for the current user
            java.util.Map<Integer, SubscriptionCheckDTO> subChecks = new java.util.HashMap<>();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isLoggedIn = authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getName());

            if (isLoggedIn) {
                String username = authentication.getName();
                UserDTO user = userService.findByUsername(username);
                if (user != null) {
                    for (SubscriptionsDTO sub : subscriptions) {
                        SubscriptionCheckDTO checkResult = transactionsService.checkSubscription(
                                user.getUserId(),
                                sub.getPrice().longValue(),
                                sub.getPrice());
                        subChecks.put(sub.getSubscriptionId(), checkResult);
                    }
                }
            } else {
                for (SubscriptionsDTO sub : subscriptions) {
                    subChecks.put(sub.getSubscriptionId(), SubscriptionCheckDTO.builder()
                            .canSubscribe(true)
                            .currentPrice(0L)
                            .discountAmount(0L)
                            .isUpgrade(false)
                            .build());
                }
            }
            model.addAttribute("subChecks", subChecks);

            return "clients/subscriptions/buysubscriptions";
        } catch (Exception e) {
            e.printStackTrace();
            return "clients/subscriptions/buysubscriptions";
        }
    }

    @GetMapping("/check-subscription")
    public ResponseEntity<SubscriptionCheckDTO> checkSubscription(
            @RequestParam("subscriptionId") Integer subscriptionId) {

        try {
            SubscriptionsDTO subscription = subscriptionsService.getSubscriptionById(subscriptionId);
            if (subscription == null) {
                return ResponseEntity.badRequest().body(
                        SubscriptionCheckDTO.builder()
                                .canSubscribe(false)
                                .message("GÃ³i khÃ´ng tá»“n táº¡i")
                                .build());
            }

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getName())) {
                // NgÆ°á» i dÃ¹ng chÆ°a Ä‘Äƒng nháººp, cho phÃ©p Ä‘Äƒng kÃ­
                return ResponseEntity.ok(
                        SubscriptionCheckDTO.builder()
                                .canSubscribe(true)
                                .currentPrice(0L)
                                .discountAmount(0L)
                                .isUpgrade(false)
                                .build());
            }

            String username = authentication.getName();
            UserDTO user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.ok(
                        SubscriptionCheckDTO.builder()
                                .canSubscribe(true)
                                .currentPrice(0L)
                                .discountAmount(0L)
                                .isUpgrade(false)
                                .build());
            }

            // Check subscription
            SubscriptionCheckDTO checkResult = transactionsService.checkSubscription(
                    user.getUserId(),
                    subscription.getPrice().longValue(),
                    subscription.getPrice());

            return ResponseEntity.ok(checkResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    SubscriptionCheckDTO.builder()
                            .canSubscribe(false)
                            .message("Lá»—i: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/pay")
    public RedirectView paySubscription(
            @RequestParam("subscriptionId") Integer subscriptionId,
            @RequestParam(value = "payment", defaultValue = "momo") String payment,
            @RequestParam(value = "discountAmount", defaultValue = "0") Long discountAmount,
            HttpSession session) {

        System.out.println("=== Pay Request ===");
        System.out.println("SubscriptionId: " + subscriptionId);
        System.out.println("Payment: " + payment);
        System.out.println("Discount: " + discountAmount);

        SubscriptionsDTO subscription = subscriptionsService.getSubscriptionById(subscriptionId);
        if (subscription == null) {
            return new RedirectView("/pricing?error=invalid_subscription");
        }

        // Get current user to check membership
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            UserDTO user = userService.findByUsername(username);

            if (user != null) {
                // âœ… CHECK: Kiá»ƒm tra xem user cÃ³ Ä‘Æ°á»£c phÃ©p downgrade khÃ´ng
                SubscriptionCheckDTO checkResult = transactionsService.checkSubscription(
                        user.getUserId(),
                        subscription.getPrice().longValue(),
                        subscription.getPrice());

                // Náº¿u canSubscribe = false (downgrade hoáº·c rule khÃ¡c bá»‹ vi pháº¡m), tá»« chá»‘i
                if (!checkResult.isCanSubscribe()) {
                    return new RedirectView("/pricing?error=cannot_downgrade");
                }

                // Store discount info vÃ o session (use passed value if available, otherwise use
                // checkResult)
                if (discountAmount > 0) {
                    session.setAttribute("discountAmount", discountAmount);
                    session.setAttribute("isUpgrade", checkResult.isUpgrade());
                } else if (checkResult.getDiscountAmount() > 0) {
                    session.setAttribute("discountAmount", checkResult.getDiscountAmount());
                    session.setAttribute("isUpgrade", checkResult.isUpgrade());
                }
            }
        }

        // Calculate final amount after discount
        Long finalAmount = subscription.getPrice().longValue() - discountAmount;
        if (finalAmount < 0)
            finalAmount = 0L;

        String amount = String.valueOf(finalAmount);
        System.out.println("Final Amount to Gateway: " + amount + "Ä‘ (Original: " + subscription.getPrice()
                + "Ä‘, Discount: " + discountAmount + "Ä‘)");

        String orderId = subscriptionId + "_" + UUID.randomUUID().toString().substring(0, 8);
        String orderInfo = "Thanh toan goi " + subscription.getName();

        try {
            if ("zalopay".equalsIgnoreCase(payment)) {
                ZaloPayPaymentDTO response = zaloPayService.createPayment(orderId, amount, orderInfo);
                if (response != null && response.getOrder_url() != null && !response.getOrder_url().isEmpty()) {
                    // Store orderId Ä‘á»ƒ check láº¡i discount khi callback
                    session.setAttribute("lastOrderId", orderId);
                    return new RedirectView(response.getOrder_url());
                }
            } else {
                MomoPaymentDTO response = momoService.createPayment(orderId, amount, orderInfo);
                if (response != null && response.getPayUrl() != null && !response.getPayUrl().isEmpty()) {
                    // Store orderId Ä‘á»ƒ check láº¡i discount khi callback
                    session.setAttribute("lastOrderId", orderId);
                    return new RedirectView(response.getPayUrl());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new RedirectView("/pricing?error=payment_failed");
    }

    /**
     * Handle free subscription - no payment required
     */
    @GetMapping("/free-start")
    public RedirectView freeSubscriptionStart(
            @RequestParam("subscriptionId") Integer subscriptionId,
            Model model) {

        try {
            SubscriptionsDTO subscription = subscriptionsService.getSubscriptionById(subscriptionId);
            if (subscription == null) {
                return new RedirectView("/pricing?error=invalid_subscription");
            }

            // Get current user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : null;

            if (username == null || "anonymousUser".equals(username)) {
                // User not logged in, redirect to login
                return new RedirectView("/login?redirect=/pricing");
            }

            // Get user from database using username
            UserDTO user = userService.findByUsername(username);
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            // âœ… CHECK: KhÃ´ng cho phÃ©p háº¡ cáººp xuá»‘ng FREE (subscription.getPrice() = 0)
            SubscriptionCheckDTO checkResult = transactionsService.checkSubscription(
                    user.getUserId(),
                    subscription.getPrice().longValue(),
                    subscription.getPrice());

            // Náº¿u canSubscribe = false (downgrade hoáº·c rule khÃ¡c bá»‹ vi pháº¡m), tá»« chá»‘i
            if (!checkResult.isCanSubscribe()) {
                return new RedirectView("/pricing?error=cannot_downgrade");
            }

            // KhÃ´ng cáº§n táº¡o transaction cho gÃ³i FREE (chá»‰ chuyá»ƒn hÆ°á»›ng)
            // transactionsService.createAndCompleteTransaction(user.getUserId(), subscriptionId, subscription.getPrice());

            // Redirect to reading page
            return new RedirectView("/?success=free_subscription");
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("/pricing?error=free_subscription_failed");
        }
    }
}




