package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.MotelAreaDTO;
import com.project.logiclayer.service.MotelAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AreaController: Cung cấp API quản lý khu trọ (Mục 2.1).
 *
 * Endpoint:
 *   GET  /api/business/areas          → Danh sách tất cả khu trọ kèm thống kê
 *   GET  /api/business/areas/{id}     → Chi tiết một khu trọ
 *   POST /api/business/areas          → Tạo khu trọ mới (chỉ HOST)
 *   PUT  /api/business/areas/{id}     → Cập nhật thông tin khu trọ (chỉ HOST)
 */
@RestController
@RequestMapping("/api/business/areas")
public class AreaController {

    @Autowired
    private MotelAreaService motelAreaService;

    /**
     * Lấy danh sách tất cả khu trọ kèm thống kê tổng quan.
     * Public — khách chưa đăng nhập vẫn xem được (dùng cho trang tìm phòng).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MotelAreaDTO>>> getAllAreas() {
        List<MotelAreaDTO> areas = motelAreaService.getAllAreasWithStats();
        return ResponseEntity.ok(ApiResponse.success(areas));
    }

    /**
     * Lấy chi tiết một khu trọ theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MotelAreaDTO>> getAreaDetail(@PathVariable Long id) {
        MotelAreaDTO area = motelAreaService.getAreaDetail(id);
        return ResponseEntity.ok(ApiResponse.success(area));
    }

    /**
     * Tạo khu trọ mới — chỉ HOST được phép.
     * @PreAuthorize kiểm tra role từ JWT token.
     */
    @PostMapping
    // @PreAuthorize("hasRole('HOST')")  // Bỏ comment khi đã cấu hình xong Spring Security
    public ResponseEntity<ApiResponse<MotelAreaDTO>> createArea(@RequestBody MotelAreaDTO dto) {
        MotelAreaDTO created = motelAreaService.createArea(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo khu trọ thành công", created));
    }

    /**
     * Cập nhật thông tin khu trọ — chỉ HOST được phép.
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<MotelAreaDTO>> updateArea(
            @PathVariable Long id,
            @RequestBody MotelAreaDTO dto) {
        MotelAreaDTO updated = motelAreaService.updateArea(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", updated));
    }
}
