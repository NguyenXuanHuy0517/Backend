package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.ContractDetailDTO;
import com.project.datalayer.dto.ContractRequestDTO;
import com.project.datalayer.entity.Contract;
import com.project.logiclayer.service.ContractBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ContractController (cập nhật hoàn chỉnh — Mục 2.5 + 2.6).
 *
 * Endpoint đầy đủ:
 *   POST  /api/business/contracts                         → Tạo hợp đồng mới
 *   GET   /api/business/contracts/{id}                   → Xem chi tiết hợp đồng [MỚI]
 *   GET   /api/business/contracts/my/{tenantId}          → Hợp đồng đang hiệu lực [MỚI]
 *   GET   /api/business/contracts/history/{tenantId}     → Lịch sử hợp đồng       [MỚI]
 *   PATCH /api/business/contracts/{id}/extend            → Gia hạn hợp đồng
 *   PATCH /api/business/contracts/{id}/terminate         → Chấm dứt sớm
 *   POST  /api/business/contracts/{id}/services/{sid}    → Đăng ký dịch vụ        [MỚI]
 *   DELETE /api/business/contracts/{id}/services/{sid}   → Hủy dịch vụ            [MỚI]
 */
@RestController
@RequestMapping("/api/business/contracts")
public class ContractController {

    @Autowired
    private ContractBusinessService contractBusinessService;

    // ─── Tạo hợp đồng ────────────────────────────────────────────────────────

    @PostMapping
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<String>> createContract(
            @RequestBody ContractRequestDTO dto) {
        Contract contract = contractBusinessService.createNewContract(dto);
        return ResponseEntity.ok(
                ApiResponse.success("Tạo hợp đồng thành công",
                        "Mã hợp đồng: " + contract.getContractCode()));
    }

    // ─── Xem chi tiết hợp đồng ───────────────────────────────────────────────

    /**
     * Xem chi tiết đầy đủ một hợp đồng theo ID.
     * HOST xem được tất cả; TENANT chỉ xem được hợp đồng của mình
     * (cần bật @PreAuthorize + lấy tenantId từ JWT để kiểm tra).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractDetailDTO>> getContractDetail(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(contractBusinessService.getContractDetail(id)));
    }

    /**
     * Người thuê xem hợp đồng ACTIVE hiện tại của mình.
     * Đây là màn hình "Hợp đồng của tôi" trên Flutter App.
     */
    @GetMapping("/my/{tenantId}")
    public ResponseEntity<ApiResponse<ContractDetailDTO>> getMyContract(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(
                ApiResponse.success(contractBusinessService.getMyActiveContract(tenantId)));
    }

    /**
     * Lịch sử tất cả hợp đồng (kể cả đã hết hạn) của một người thuê.
     */
    @GetMapping("/history/{tenantId}")
    public ResponseEntity<ApiResponse<List<ContractDetailDTO>>> getContractHistory(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(
                ApiResponse.success(contractBusinessService.getContractsByTenant(tenantId)));
    }

    // ─── Gia hạn & chấm dứt ──────────────────────────────────────────────────

    @PatchMapping("/{id}/extend")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<Void>> extendContract(
            @PathVariable Long id,
            @RequestParam String newEndDate) {
        contractBusinessService.extendContract(id, LocalDate.parse(newEndDate));
        return ResponseEntity.ok(ApiResponse.success("Gia hạn hợp đồng thành công", null));
    }

    @PatchMapping("/{id}/terminate")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<Void>> terminateContract(@PathVariable Long id) {
        contractBusinessService.terminateContract(id);
        return ResponseEntity.ok(ApiResponse.success("Đã chấm dứt hợp đồng", null));
    }

    // ─── Đăng ký / hủy dịch vụ (Mục 2.5) ────────────────────────────────────

    /**
     * Người thuê đăng ký thêm một dịch vụ vào hợp đồng đang ACTIVE.
     *
     * Ví dụ: Đăng ký thêm dịch vụ Wifi (serviceId = 3) cho hợp đồng 12:
     *   POST /api/business/contracts/12/services/3
     */
    @PostMapping("/{id}/services/{serviceId}")
    public ResponseEntity<ApiResponse<ContractDetailDTO>> addService(
            @PathVariable Long id,
            @PathVariable Long serviceId) {
        ContractDetailDTO result = contractBusinessService.addService(id, serviceId);
        return ResponseEntity.ok(
                ApiResponse.success("Đăng ký dịch vụ thành công", result));
    }

    /**
     * Người thuê hủy một dịch vụ đang đăng ký trong hợp đồng.
     *
     * Ví dụ: Hủy dịch vụ Wifi (serviceId = 3) khỏi hợp đồng 12:
     *   DELETE /api/business/contracts/12/services/3
     */
    @DeleteMapping("/{id}/services/{serviceId}")
    public ResponseEntity<ApiResponse<ContractDetailDTO>> removeService(
            @PathVariable Long id,
            @PathVariable Long serviceId) {
        ContractDetailDTO result = contractBusinessService.removeService(id, serviceId);
        return ResponseEntity.ok(
                ApiResponse.success("Hủy dịch vụ thành công", result));
    }
}