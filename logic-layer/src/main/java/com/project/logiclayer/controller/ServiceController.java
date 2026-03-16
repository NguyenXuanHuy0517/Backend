package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.ServiceDTO;
import com.project.logiclayer.service.ServiceBusinessService;
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

    @Autowired
    private ServiceBusinessService serviceBusinessService;

    /**
     * Lấy tất cả dịch vụ — dùng cho màn hình đăng ký dịch vụ của người thuê.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices() {
        List<ServiceDTO> services = serviceBusinessService.getAllActiveServices();
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    /**
     * Lấy dịch vụ theo khu trọ — mỗi khu có thể có bộ dịch vụ riêng.
     */
    @GetMapping("/area/{areaId}")
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getServicesByArea(
            @PathVariable Long areaId) {
        List<ServiceDTO> services = serviceBusinessService.getServicesByArea(areaId);
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    /**
     * Chủ trọ tạo dịch vụ mới cho khu trọ.
     * Body ví dụ: { "serviceName": "Wifi", "price": 100000, "unitName": "Tháng", "areaId": 1 }
     */
    @PostMapping
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@RequestBody ServiceDTO dto) {
        ServiceDTO created = serviceBusinessService.createService(dto);
        return ResponseEntity.ok(ApiResponse.success("Thêm dịch vụ thành công", created));
    }

    /**
     * Cập nhật thông tin dịch vụ (tên, giá, mô tả).
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(
            @PathVariable Long id,
            @RequestBody ServiceDTO dto) {
        ServiceDTO updated = serviceBusinessService.updateService(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật dịch vụ thành công", updated));
    }

    /**
     * Xóa dịch vụ — chỉ xóa được nếu không có hợp đồng nào đang dùng dịch vụ này.
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        serviceBusinessService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa dịch vụ thành công", null));
    }
}
