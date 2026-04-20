package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.config.ZaloPayConfig;
import com.group1.mangaflowweb.dto.zalopay.ZaloPayPaymentResponse;
import com.group1.mangaflowweb.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloPayServiceImpl implements ZaloPayService {

    private final ZaloPayConfig zaloPayConfig;

    @Override
    public ZaloPayPaymentResponse createPayment(String orderId, String amount, String orderInfo) {
        try {
            String embedDataString = "{\"redirecturl\":\"" + zaloPayConfig.getReturnUrl() + "\"}";

            String safeOrderInfo = orderInfo.replace("\"", "\\\"");
            String itemString = "[{\"itemid\":\"" + orderId + "\",\"itemname\":\"" + safeOrderInfo + "\",\"itemprice\":" + amount + ",\"itemquantity\":1}]";


            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            // Keep the orderId format for callback parsing (subscriptionId_xxxxx)
            String transId = sdf.format(new java.util.Date()) + "_" + orderId;

            long appTime = System.currentTimeMillis();
            String appUser = "MangaFlowWeb";

            String data = zaloPayConfig.getAppId() + "|" + transId + "|" + appUser + "|" + amount + "|"
                    + appTime + "|" + embedDataString + "|" + itemString;

            String mac = generateHmacSHA256(data, zaloPayConfig.getKey1());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("app_id", zaloPayConfig.getAppId());
            body.add("app_user", appUser);
            body.add("app_time", String.valueOf(appTime));
            body.add("amount", amount);
            body.add("app_trans_id", transId);
            body.add("item", itemString);
            body.add("description", orderInfo);
            body.add("embed_data", embedDataString);
            body.add("mac", mac);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<ZaloPayPaymentResponse> response = restTemplate.postForEntity(
                    zaloPayConfig.getEndpoint(),
                    request,
                    ZaloPayPaymentResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating ZaloPay payment", e);
            throw new RuntimeException("Error calling ZaloPay API", e);
        }
    }

    private String generateHmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }
}
