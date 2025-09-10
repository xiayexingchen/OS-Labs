package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源，这里设置为所有源，可以根据需要限制特定域名
        config.addAllowedOriginPattern("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许的请求方法
        config.addAllowedMethod("*");
        
        // 允许携带凭证（如Cookie）
        config.setAllowCredentials(true);
        
        // 2. 创建CORS源配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 为所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        
        // 3. 创建并返回CORS过滤器
        return new CorsFilter(source);
    }
}