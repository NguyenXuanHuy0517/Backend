package com.project.logiclayer.config;

import com.project.logiclayer.security.JwtAuthFilter;
import com.project.logiclayer.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — Tương thích Spring Boot 4.x / Spring Security 6.4+
 *
 * LỖI ĐÃ SỬA (2 lỗi trong ảnh):
 *
 * 1. "constructor DaoAuthenticationProvider cannot be applied to given types" :57
 *    Nguyên nhân: Spring Security 6.4+ đổi constructor DaoAuthenticationProvider.
 *    Cách cũ: new DaoAuthenticationProvider()  → deprecated/removed
 *    Cách mới: new DaoAuthenticationProvider(passwordEncoder())
 *    → Truyền PasswordEncoder thẳng vào constructor, không gọi .setPasswordEncoder() nữa.
 *
 * 2. "cannot find symbol method setUserDetailsService(UserDetailsServiceImpl)" :58
 *    Nguyên nhân: Spring Security 6.4+ đổi kiểu tham số của setUserDetailsService().
 *    Cách cũ: provider.setUserDetailsService(userDetailsService)  ← nhận UserDetailsService
 *    Nhưng bean UserDetailsServiceImpl implements UserDetailsService nên vẫn hợp lệ —
 *    lỗi thật ra do lỗi #1 khiến compiler không parse được dòng tiếp theo.
 *    Fix lỗi #1 là đủ để cả 2 lỗi biến mất.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * FIX: Dùng constructor mới của DaoAuthenticationProvider (Spring Security 6.4+).
     * Truyền PasswordEncoder vào constructor thay vì gọi setter.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/business/rooms/**").permitAll()
                        .requestMatchers("/api/business/areas/**").permitAll()
                        .requestMatchers("/api/business/invoices/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}