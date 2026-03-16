package com.project.logiclayer.controller;

import com.project.logiclayer.service.AuthService;
import com.project.datalayer.dto.UserDTO;
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

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String phone = loginRequest.get("phoneNumber");
            String pass = loginRequest.get("password");

            // Trả về Map chứa { user: UserDTO, token: String }
            Map<String, Object> authData = authService.login(phone, pass);
            return ResponseEntity.ok(authData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> registerRequest) {
        try {
            UserDTO dto = UserDTO.builder()
                    .phoneNumber((String) registerRequest.get("phoneNumber"))
                    .fullName((String) registerRequest.get("fullName"))
                    .email((String) registerRequest.get("email"))
                    .idCardNumber((String) registerRequest.get("idCardNumber"))
                    .build();

            String password = (String) registerRequest.get("password");
            UserDTO savedUser = authService.registerTenant(dto, password);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}