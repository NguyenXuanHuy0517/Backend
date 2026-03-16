package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.ContractDetailDTO;
import com.project.datalayer.dto.ContractRequestDTO;
import com.project.datalayer.entity.Contract;
import com.project.logiclayer.service.ContractBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    @Autowired
    private ContractBusinessService contractBusinessService;

    // ─── Tạo hợp đồng ────────────────────────────────────────────────────────

    @PostMapping
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<String>> createContract(
            @RequestBody ContractRequestDTO dto) {
        logger.info("[CONTRACT] POST /api/business/contracts - Creating new contract for tenantId: {}, roomId: {}",
                dto.getTenantId(), dto.getRoomId());
        try {
            Contract contract = contractBusinessService.createNewContract(dto);
            logger.info("[CONTRACT] POST /api/business/contracts - Contract created successfully with code: {}", contract.getContractCode());
            return ResponseEntity.ok(
                    ApiResponse.success("Tạo hợp đồng thành công",
                            "Mã hợp đồng: " + contract.getContractCode()));
        } catch (Exception e) {
            logger.error("[CONTRACT] POST /api/business/contracts - Error creating contract: {}", e.getMessage(), e);
            throw e;
        }
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
        logger.info("[CONTRACT] GET /api/business/contracts/{} - Fetching contract detail", id);
        try {
            ContractDetailDTO detail = contractBusinessService.getContractDetail(id);
            logger.info("[CONTRACT] GET /api/business/contracts/{} - Contract detail retrieved", id);
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (Exception e) {
            logger.error("[CONTRACT] GET /api/business/contracts/{} - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Người thuê xem hợp đồng ACTIVE hiện tại của mình.
     * Đây là màn hình "Hợp đồng của tôi" trên Flutter App.
     */
    @GetMapping("/my/{tenantId}")
    public ResponseEntity<ApiResponse<ContractDetailDTO>> getMyContract(
            @PathVariable Long tenantId) {
        logger.info("[CONTRACT] GET /api/business/contracts/my/{} - Fetching active contract for tenant", tenantId);
        try {
            ContractDetailDTO contract = contractBusinessService.getMyActiveContract(tenantId);
            logger.info("[CONTRACT] GET /api/business/contracts/my/{} - Active contract retrieved", tenantId);
            return ResponseEntity.ok(ApiResponse.success(contract));
        } catch (Exception e) {
            logger.error("[CONTRACT] GET /api/business/contracts/my/{} - Error: {}", tenantId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lịch sử tất cả hợp đồng (kể cả đã hết hạn) của một người thuê.
     */
    @GetMapping("/history/{tenantId}")
    public ResponseEntity<ApiResponse<List<ContractDetailDTO>>> getContractHistory(
            @PathVariable Long tenantId) {
        logger.info("[CONTRACT] GET /api/business/contracts/history/{} - Fetching contract history for tenant", tenantId);
        try {
            List<ContractDetailDTO> contracts = contractBusinessService.getContractsByTenant(tenantId);
            logger.info("[CONTRACT] GET /api/business/contracts/history/{} - Retrieved {} contracts", tenantId, contracts.size());
            return ResponseEntity.ok(ApiResponse.success(contracts));
        } catch (Exception e) {
            logger.error("[CONTRACT] GET /api/business/contracts/history/{} - Error: {}", tenantId, e.getMessage(), e);
            throw e;
        }
    }

    // ─── Gia hạn & chấm dứt ──────────────────────────────────────────────────

    @PatchMapping("/{id}/extend")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<Void>> extendContract(
            @PathVariable Long id,
            @RequestParam String newEndDate) {
        logger.info("[CONTRACT] PATCH /api/business/contracts/{}/extend - Extending contract to date: {}", id, newEndDate);
        try {
            contractBusinessService.extendContract(id, LocalDate.parse(newEndDate));
            logger.info("[CONTRACT] PATCH /api/business/contracts/{}/extend - Contract extended successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Gia hạn hợp đồng thành công", null));
        } catch (Exception e) {
            logger.error("[CONTRACT] PATCH /api/business/contracts/{}/extend - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/{id}/terminate")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<Void>> terminateContract(@PathVariable Long id) {
        logger.info("[CONTRACT] PATCH /api/business/contracts/{}/terminate - Terminating contract", id);
        try {
            contractBusinessService.terminateContract(id);
            logger.info("[CONTRACT] PATCH /api/business/contracts/{}/terminate - Contract terminated successfully", id);
            return ResponseEntity.ok(ApiResponse.success("Đã chấm dứt hợp đồng", null));
        } catch (Exception e) {
            logger.error("[CONTRACT] PATCH /api/business/contracts/{}/terminate - Error: {}", id, e.getMessage(), e);
            throw e;
        }
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
        logger.info("[CONTRACT] POST /api/business/contracts/{}/services/{} - Adding service to contract", id, serviceId);
        try {
            ContractDetailDTO result = contractBusinessService.addService(id, serviceId);
            logger.info("[CONTRACT] POST /api/business/contracts/{}/services/{} - Service added successfully", id, serviceId);
            return ResponseEntity.ok(
                    ApiResponse.success("Đăng ký dịch vụ thành công", result));
        } catch (Exception e) {
            logger.error("[CONTRACT] POST /api/business/contracts/{}/services/{} - Error: {}", id, serviceId, e.getMessage(), e);
            throw e;
        }
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
        logger.info("[CONTRACT] DELETE /api/business/contracts/{}/services/{} - Removing service from contract", id, serviceId);
        try {
            ContractDetailDTO result = contractBusinessService.removeService(id, serviceId);
            logger.info("[CONTRACT] DELETE /api/business/contracts/{}/services/{} - Service removed successfully", id, serviceId);
            return ResponseEntity.ok(
                    ApiResponse.success("Hủy dịch vụ thành công", result));
        } catch (Exception e) {
            logger.error("[CONTRACT] DELETE /api/business/contracts/{}/services/{} - Error: {}", id, serviceId, e.getMessage(), e);
            throw e;
        }
    }
}