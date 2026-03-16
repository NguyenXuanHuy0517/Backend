package com.project.logiclayer.security;

import com.project.datalayer.entity.User;
import com.project.datalayer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * UserDetailsServiceImpl: Implement interface UserDetailsService của Spring Security.
 *
 * Mục đích: Spring Security cần biết cách load thông tin user từ DB khi
 * xác thực. Class này chính là cầu nối giữa Spring Security và UserRepository.
 *
 * Luồng hoạt động trong JwtAuthFilter:
 *   token → extractPhoneNumber → loadUserByUsername(phone) → UserDetails
 *   → Spring Security biết user này là ai, có quyền gì
 *
 * Lưu ý về "username" trong Spring Security:
 * Spring Security dùng khái niệm "username" làm định danh duy nhất.
 * Trong hệ thống này, số điện thoại đóng vai trò đó — phương thức
 * loadUserByUsername nhận vào phone number, không phải tên đăng nhập.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user theo số điện thoại (được dùng làm "username" trong hệ thống).
     *
     * @param phoneNumber Số điện thoại người dùng
     * @return UserDetails chứa: phone, password hash, và danh sách quyền (roles)
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy user với số điện thoại: " + phoneNumber));

        // Lấy tên role để gán quyền cho Spring Security
        // Quy ước: Spring Security yêu cầu authority bắt đầu bằng "ROLE_"
        // Ví dụ: role_name = "HOST" → authority = "ROLE_HOST"
        String roleName = (user.getRole() != null)
                ? "ROLE_" + user.getRole().getRoleName()
                : "ROLE_TENANT"; // Mặc định nếu chưa gán role

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhoneNumber())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
                // Khóa tài khoản nếu isActive = false
                .accountLocked(Boolean.FALSE.equals(user.getIsActive()))
                .build();
    }
}