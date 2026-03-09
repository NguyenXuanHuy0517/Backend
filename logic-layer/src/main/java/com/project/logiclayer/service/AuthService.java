package com.project.logiclayer.service;

import com.project.logiclayer.security.JwtUtils;
import com.project.datalayer.dto.UserDTO;
import com.project.datalayer.entity.User;
import com.project.datalayer.mapper.UserMapper;
import com.project.datalayer.repository.RoleRepository;
import com.project.datalayer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Đăng nhập thành công sẽ trả về cả User thông tin và Token.
     */
    public Map<String, Object> login(String phoneNumber, String password) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Số điện thoại không tồn tại."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu không chính xác.");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản đã bị khóa.");
        }

        // Tạo Token
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";
        String token = jwtUtils.generateToken(user.getPhoneNumber(), roleName);

        // Đóng gói kết quả
        Map<String, Object> response = new HashMap<>();
        response.put("user", userMapper.toDTO(user));
        response.put("token", token);

        return response;
    }

    public UserDTO registerTenant(UserDTO registerDTO, String rawPassword) {
        if (userRepository.existsByPhoneNumber(registerDTO.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại này đã được đăng ký.");
        }

        User newUser = new User();
        newUser.setPhoneNumber(registerDTO.getPhoneNumber());
        newUser.setFullName(registerDTO.getFullName());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setIdCardNumber(registerDTO.getIdCardNumber());
        newUser.setIsActive(true);
        newUser.setPasswordHash(passwordEncoder.encode(rawPassword));

        // Set role to TENANT
        roleRepository.findByRoleName("TENANT").ifPresent(newUser::setRole);

        User savedUser = userRepository.save(newUser);
        return userMapper.toDTO(savedUser);
    }
}