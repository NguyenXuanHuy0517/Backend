package com.project.logiclayer.controller;

import com.project.datalayer.dto.TenantDTO;
import com.project.datalayer.dto.UserDTO;
import com.project.logiclayer.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TenantController: Quản lý người thuê cho Host.
 * Base path: /api/business/tenants
 */
@RestController
@RequestMapping("/api/business/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    /**
     * GET /api/business/tenants/by-host/{hostId}
     * Lấy danh sách tất cả người thuê có hợp đồng trong khu trọ của host.
     */
    @GetMapping("/by-host/{hostId}")
    public ResponseEntity<List<TenantDTO>> getTenantsByHost(@PathVariable Long hostId) {
        return ResponseEntity.ok(tenantService.getTenantsOfHost(hostId));
    }

    /**
     * GET /api/business/tenants/{id}
     * Xem chi tiết một người thuê theo userId.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TenantDTO> getTenantDetail(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantDetail(id));
    }

    /**
     * POST /api/business/tenants
     * Host tạo tài khoản tenant mới.
     * Body: { fullName, phoneNumber, email, idCardNumber }
     * Password mặc định = phoneNumber.
     */
    @PostMapping
    public ResponseEntity<TenantDTO> createTenant(@RequestBody UserDTO userDTO) {
        TenantDTO created = tenantService.createTenant(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/business/tenants/{id}
     * Cập nhật thông tin cơ bản của tenant (tên, email, CMND).
     */
    @PutMapping("/{id}")
    public ResponseEntity<TenantDTO> updateTenant(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(tenantService.updateTenant(id, userDTO));
    }

    /**
     * PATCH /api/business/tenants/{id}/toggle-active
     * Khoá hoặc mở khoá tài khoản tenant.
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<TenantDTO> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.toggleActive(id));
    }
}