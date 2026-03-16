package com.project.logiclayer.controller;

import com.project.logiclayer.service.AuthService;
import com.project.datalayer.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController: Cung cấp API xác thực có kèm JWT cho Flutter.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String phone = loginRequest.get("phoneNumber");
            logger.info("[AUTH] POST /api/auth/login - Attempting login for phone: {}", phone);
            String pass = loginRequest.get("password");

            // Trả về Map chứa { user: UserDTO, token: String }
            Map<String, Object> authData = authService.login(phone, pass);
            logger.info("[AUTH] POST /api/auth/login - Login successful for phone: {}", phone);
            return ResponseEntity.ok(authData);
        } catch (RuntimeException e) {
            logger.error("[AUTH] POST /api/auth/login - Login failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> registerRequest) {
        try {
            String phone = (String) registerRequest.get("phoneNumber");
            String email = (String) registerRequest.get("email");
            logger.info("[AUTH] POST /api/auth/register - Attempting registration for phone: {}, email: {}", phone, email);
            
            UserDTO dto = UserDTO.builder()
                    .phoneNumber(phone)
                    .fullName((String) registerRequest.get("fullName"))
                    .email(email)
                    .idCardNumber((String) registerRequest.get("idCardNumber"))
                    .build();

            String password = (String) registerRequest.get("password");
            UserDTO savedUser = authService.registerTenant(dto, password);
            logger.info("[AUTH] POST /api/auth/register - Registration successful for phone: {}", phone);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            logger.error("[AUTH] POST /api/auth/register - Registration failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}