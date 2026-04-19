package com.group1.mangaflowweb.dto.zalopay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZaloPayPaymentResponse {
    private Integer return_code;
    private String return_message;
    private Integer sub_return_code;
    private String sub_return_message;
    private String order_url;
    private String zp_trans_token;
}
