package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.config.MomoConfig;
import com.group1.mangaflowweb.dto.momo.MomoPaymentRequest;
import com.group1.mangaflowweb.dto.momo.MomoPaymentResponse;
import com.group1.mangaflowweb.service.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoServiceImpl implements MomoService {

    private final MomoConfig momoConfig;

    @Override
    public MomoPaymentResponse createPayment(String orderId, String amount, String orderInfo) {
        String requestId = UUID.randomUUID().toString();
        String extraData = "";

        String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + momoConfig.getNotifyUrl() +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&redirectUrl=" + momoConfig.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=" + momoConfig.getRequestType();

        String signature = generateHmacSHA256(rawSignature, momoConfig.getSecretKey());

        MomoPaymentRequest request = new MomoPaymentRequest(
                momoConfig.getPartnerCode(),
                momoConfig.getAccessKey(),
                requestId,
                amount,
                orderId,
                orderInfo,
                momoConfig.getReturnUrl(),
                momoConfig.getNotifyUrl(),
                momoConfig.getRequestType(),
                extraData,
                "vi",
                signature
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoPaymentRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<MomoPaymentResponse> response = restTemplate.postForEntity(
                    momoConfig.getEndpoint(),
                    entity,
                    MomoPaymentResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating Momo payment", e);
            throw new RuntimeException("Error calling Momo API", e);
        }
    }

    private String generateHmacSHA256(String data, String key) {
        try {
            if (data == null || data.isEmpty()) {
                log.error("Data for HMAC generation is null or empty");
                throw new IllegalArgumentException("Data cannot be null or empty");
            }
            if (key == null || key.isEmpty()) {
                log.error("Secret key is null or empty. MomoConfig: {}", momoConfig);
                throw new IllegalArgumentException("Secret key cannot be null or empty");
            }

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
            log.error("Failed to calculate HMAC-SHA256: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }
}
