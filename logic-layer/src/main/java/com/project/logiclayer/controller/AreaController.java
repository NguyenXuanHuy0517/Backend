package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.MotelAreaDTO;
import com.project.logiclayer.service.MotelAreaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AreaController: Cung cấp API quản lý khu trọ (Mục 2.1).
 * <p>
 * Endpoint:
 *   GET  /api/business/areas          → Danh sách tất cả khu trọ kèm thống kê
 *   GET  /api/business/areas/{id}     → Chi tiết một khu trọ
 *   POST /api/business/areas          → Tạo khu trọ mới (chỉ HOST)
 *   PUT  /api/business/areas/{id}     → Cập nhật thông tin khu trọ (chỉ HOST)
 */
@RestController
@RequestMapping("/api/business/areas")
public class AreaController {

    private static final Logger logger = LoggerFactory.getLogger(AreaController.class);

    private final MotelAreaService motelAreaService;

    @Autowired
    public AreaController(MotelAreaService motelAreaService) {
        this.motelAreaService = motelAreaService;
    }

    /**
     * Lấy danh sách tất cả khu trọ kèm thống kê tổng quan.
     * Public — khách chưa đăng nhập vẫn xem được (dùng cho trang tìm phòng).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MotelAreaDTO>>> getAllAreas() {
        logger.info("[AREA] GET /api/business/areas - Fetching all areas");
        try {
            List<MotelAreaDTO> areas = motelAreaService.getAllAreasWithStats();
            logger.info("[AREA] GET /api/business/areas - Retrieved {} areas", areas.size());
            return ResponseEntity.ok(ApiResponse.success(areas));
        } catch (Exception e) {
            logger.error("[AREA] GET /api/business/areas - Error retrieving areas: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lấy chi tiết một khu trọ theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MotelAreaDTO>> getAreaDetail(@PathVariable Long id) {
        logger.info("[AREA] GET /api/business/areas/{} - Fetching area detail", id);
        try {
            MotelAreaDTO area = motelAreaService.getAreaDetail(id);
            logger.info("[AREA] GET /api/business/areas/{} - Area detail retrieved", id);
            return ResponseEntity.ok(ApiResponse.success(area));
        } catch (Exception e) {
            logger.error("[AREA] GET /api/business/areas/{} - Error retrieving area: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Tạo khu trọ mới — chỉ HOST được phép.
     */
    @PostMapping
    // @PreAuthorize("hasRole('HOST')")  // Bỏ comment khi đã cấu hình xong Spring Security
    public ResponseEntity<ApiResponse<MotelAreaDTO>> createArea(@RequestBody MotelAreaDTO dto) {
        logger.info("[AREA] POST /api/business/areas - Creating new area");
        try {
            MotelAreaDTO created = motelAreaService.createArea(dto);
            logger.info("[AREA] POST /api/business/areas - Area created successfully");
            return ResponseEntity.ok(ApiResponse.success("Tạo khu trọ thành công", created));
        } catch (Exception e) {
            logger.error("[AREA] POST /api/business/areas - Error creating area: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cập nhật thông tin khu trọ — chỉ HOST được phép.
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<MotelAreaDTO>> updateArea(
            @PathVariable Long id,
            @RequestBody MotelAreaDTO dto) {
        logger.info("[AREA] PUT /api/business/areas/{} - Updating area", id);
        try {
            MotelAreaDTO updated = motelAreaService.updateArea(id, dto);
            logger.info("[AREA] PUT /api/business/areas/{} - Area updated successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", updated));
        } catch (Exception e) {
            logger.error("[AREA] PUT /api/business/areas/{} - Error updating area: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
