package com.group1.mangaflowweb.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dfh8o4dh4",
                "api_key", "724171536555544",
                "api_secret", "7jHUXHJ2dXGGDK6M119uJhVs3Ls"
        ));
    }
}
