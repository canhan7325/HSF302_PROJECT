package com.group1.mangaflowweb.config;

import com.group1.mangaflowweb.security.AdminRedirectInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminRedirectInterceptor adminRedirectInterceptor;

    public WebConfig(AdminRedirectInterceptor adminRedirectInterceptor) {
        this.adminRedirectInterceptor = adminRedirectInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminRedirectInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/api/auth/**");
    }
}
