package com.group1.mangaflowweb.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zalopay")
@Data
public class ZaloPayConfig {
    private String appId;
    private String key1;
    private String key2;
    private String endpoint;
    private String returnUrl;
}
