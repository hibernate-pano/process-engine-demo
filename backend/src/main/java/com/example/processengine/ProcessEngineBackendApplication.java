package com.example.processengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 应用主入口
 */
@SpringBootApplication
public class ProcessEngineBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessEngineBackendApplication.class, args);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域进行跨域访问，生产环境请根据实际需求配置
        config.addAllowedOrigin("http://localhost:5173"); // 您的前端地址
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有HTTP方法 (GET, POST, PUT, DELETE, etc.)
        config.setAllowCredentials(true); // 允许发送Cookie
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
