package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.ServiceDTO;
import com.project.logiclayer.service.ServiceBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ServiceController: API quản lý dịch vụ (Mục 2.5).
 *
 * Endpoint:
 *   GET   /api/business/services              → Lấy tất cả dịch vụ đang hoạt động
 *   GET   /api/business/services/area/{id}    → Lấy dịch vụ theo khu trọ
 *   POST  /api/business/services              → Chủ trọ thêm dịch vụ mới
 *   PUT   /api/business/services/{id}         → Cập nhật dịch vụ (tên, giá, đơn vị)
 *   DELETE /api/business/services/{id}        → Xóa/vô hiệu hóa dịch vụ
 */
@RestController
@RequestMapping("/api/business/services")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceBusinessService serviceBusinessService;

    /**
     * Lấy tất cả dịch vụ — dùng cho màn hình đăng ký dịch vụ của người thuê.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {
        logger.info("[SERVICE] GET /api/business/services - Fetching all active services");
        try {
            List<ServiceDTO> services = serviceBusinessService.getAllActiveServices();
            logger.info("[SERVICE] GET /api/business/services - Retrieved {} services", services.size());
            return ResponseEntity.ok(ApiResponse.success(services));
        } catch (Exception e) {
            logger.error("[SERVICE] GET /api/business/services - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lấy dịch vụ theo khu trọ — mỗi khu có thể có bộ dịch vụ riêng.
     */
    @GetMapping("/area/{areaId}")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getServicesByArea(
            @PathVariable Long areaId) {
        logger.info("[SERVICE] GET /api/business/services/area/{} - Fetching services for area", areaId);
        try {
            List<ServiceDTO> services = serviceBusinessService.getServicesByArea(areaId);
            logger.info("[SERVICE] GET /api/business/services/area/{} - Retrieved {} services", areaId, services.size());
            return ResponseEntity.ok(ApiResponse.success(services));
        } catch (Exception e) {
            logger.error("[SERVICE] GET /api/business/services/area/{} - Error: {}", areaId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Chủ trọ tạo dịch vụ mới cho khu trọ.
     * Body ví dụ: { "serviceName": "Wifi", "price": 100000, "unitName": "Tháng", "areaId": 1 }
     */
    @PostMapping
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@RequestBody ServiceDTO dto) {
        logger.info("[SERVICE] POST /api/business/services - Creating service: {}, price: {}", dto.getServiceName(), dto.getPrice());
        try {
            ServiceDTO created = serviceBusinessService.createService(dto);
            logger.info("[SERVICE] POST /api/business/services - Service created successfully with ID: {}", created.getServiceId());
            return ResponseEntity.ok(ApiResponse.success("Thêm dịch vụ thành công", created));
        } catch (Exception e) {
            logger.error("[SERVICE] POST /api/business/services - Error creating service: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cập nhật thông tin dịch vụ (tên, giá, mô tả).
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(
            @PathVariable Long id,
            @RequestBody ServiceDTO dto) {
        logger.info("[SERVICE] PUT /api/business/services/{} - Updating service: {}", id, dto.getServiceName());
        try {
            ServiceDTO updated = serviceBusinessService.updateService(id, dto);
            logger.info("[SERVICE] PUT /api/business/services/{} - Service updated successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật dịch vụ thành công", updated));
        } catch (Exception e) {
            logger.error("[SERVICE] PUT /api/business/services/{} - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Xóa dịch vụ — chỉ xóa được nếu không có hợp đồng nào đang dùng dịch vụ này.
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        logger.info("[SERVICE] DELETE /api/business/services/{} - Deleting service", id);
        try {
            serviceBusinessService.deleteService(id);
            logger.info("[SERVICE] DELETE /api/business/services/{} - Service deleted successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Xóa dịch vụ thành công", null));
        } catch (Exception e) {
            logger.error("[SERVICE] DELETE /api/business/services/{} - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
