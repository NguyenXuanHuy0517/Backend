package com.project.logiclayer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter: Chặn mọi HTTP request, đọc token JWT từ header Authorization,
 * xác thực và đặt thông tin người dùng vào SecurityContext.
 *
 * Luồng hoạt động:
 * 1. Request đến → filter đọc header "Authorization: Bearer <token>"
 * 2. Tách lấy <token>, gọi JwtUtils để xác thực chữ ký và hạn sử dụng
 * 3. Nếu hợp lệ → load UserDetails từ DB, đặt vào SecurityContextHolder
 * 4. Spring Security cho phép request đi tiếp vào Controller
 *
 * Kế thừa OncePerRequestFilter để đảm bảo filter chỉ chạy 1 lần/request,
 * tránh trường hợp Spring gọi filter 2 lần trong một số cấu hình phức tạp.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Đọc header Authorization
        String authHeader = request.getHeader("Authorization");

        // Nếu không có header hoặc không bắt đầu bằng "Bearer ", bỏ qua (request public)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Tách token (bỏ phần "Bearer " ở đầu)
        String token = authHeader.substring(7);
        String phoneNumber = null;

        try {
            phoneNumber = jwtUtils.extractPhoneNumber(token);
        } catch (Exception e) {
            // Token không hợp lệ hoặc hết hạn → bỏ qua, Spring Security sẽ trả 401
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Nếu lấy được phoneNumber và chưa có authentication trong context
        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);

            // 4. Xác thực token có hợp lệ với user này không
            if (jwtUtils.validateToken(token, userDetails.getUsername())) {

                // 5. Tạo Authentication object và đặt vào SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()  // Quyền hạn (ROLE_HOST, ROLE_TENANT...)
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Chuyển tiếp request xuống filter tiếp theo
        filterChain.doFilter(request, response);
    }
}