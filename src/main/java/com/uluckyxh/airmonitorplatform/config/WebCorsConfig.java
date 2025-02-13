package com.uluckyxh.airmonitorplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有请求都支持跨域
                .allowedOrigins("*")  // 允许所有源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许所有请求方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(false)  // 设置为false
                .maxAge(3600); // 1小时内不需要再预检（发OPTIONS请求）
    }

}
