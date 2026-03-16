package com.project.logiclayer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CorsConfig: Cấu hình Cross-Origin Resource Sharing (CORS).
 *
 * Vấn đề CORS là gì?
 * Trình duyệt và Flutter Web chặn request từ origin A (localhost:3000)
 * đến server B (localhost:8081) theo chính sách same-origin mặc định.
 * Server phải chủ động cho phép các origin bên ngoài bằng CORS headers.
 *
 * Flutter Mobile (Android/iOS) KHÔNG bị chặn bởi CORS vì không chạy trong trình duyệt.
 * Tuy nhiên vẫn nên cấu hình để hỗ trợ Flutter Web và Postman testing.
 *
 * Trong production: thay "*" bằng domain cụ thể của ứng dụng Flutter Web.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép gửi cookie/credential (cần thiết nếu dùng thêm session sau này)
        config.setAllowCredentials(true);

        // Danh sách origin được phép — thêm domain Flutter Web khi deploy
        config.addAllowedOriginPattern("*"); // Dùng pattern thay vì "*" khi allowCredentials=true

        // Cho phép tất cả HTTP headers (bao gồm Authorization cho JWT)
        config.addAllowedHeader("*");

        // Cho phép tất cả HTTP method
        config.addAllowedMethod("*");

        // Cache preflight request trong 3600 giây (tránh OPTIONS request liên tục)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Áp dụng cho tất cả path

        return new CorsFilter(source);
    }
}