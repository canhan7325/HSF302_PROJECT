package com.group1.mangaflowweb.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZaloPayPaymentResponse {
    @JsonProperty("return_code")
    private Integer return_code;
    @JsonProperty("return_message")
    private String return_message;
    @JsonProperty("sub_return_code")
    private Integer sub_return_code;
    @JsonProperty("sub_return_message")
    private String sub_return_message;
    @JsonProperty("order_url")
    private String order_url;
    @JsonProperty("zp_trans_token")
    private String zp_trans_token;
}
