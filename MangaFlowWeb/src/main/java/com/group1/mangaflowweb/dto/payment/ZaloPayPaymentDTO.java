package com.group1.mangaflowweb.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayPaymentDTO {
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

    @JsonProperty("order_token")
    private String order_token;

    @JsonProperty("qr_code")
    private String qr_code;
}
