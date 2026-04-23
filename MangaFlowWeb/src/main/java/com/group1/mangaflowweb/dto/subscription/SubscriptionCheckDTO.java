package com.group1.mangaflowweb.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCheckDTO {
    private boolean canSubscribe;  // true nếu có thể đăng kí
    private String message;        // message nếu không thể đăng kí (duplicate)
    private Long currentPrice;     // giá gói hiện tại của user
    private Long discountAmount;   // giảm giá nếu upgrade từ Bạc → Vàng
    private boolean isUpgrade;     // true nếu đang upgrade
}

