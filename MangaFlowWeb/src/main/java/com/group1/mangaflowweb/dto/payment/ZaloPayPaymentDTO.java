package com.group1.mangaflowweb.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayPaymentDTO {
    private Integer return_code;
    private String return_message;
    private Integer sub_return_code;
    private String sub_return_message;
    private String order_url;
    private String order_token;
    private String qr_code;
}
