package com.theone.tobackend.security.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // localhost:8080/uploads/** → C:/StarterBE/to-backend/uploads/ 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/StarterBE/to-backend/uploads/");
    }
}

