package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * ContractDetailDTO: Chi tiết đầy đủ một hợp đồng thuê (Mục 2.6).
 *
 * Dùng cho:
 *   GET /api/business/contracts/{id}        → Chủ trọ xem chi tiết
 *   GET /api/business/contracts/my          → Người thuê xem hợp đồng của mình
 *
 * Khác ContractRequestDTO (dùng để TẠO hợp đồng):
 *   ContractDetailDTO dùng để ĐỌC — chứa thêm thông tin tenant, room, services
 *   và các trường tính toán như daysUntilExpiry.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetailDTO {

    private Long contractId;
    private String contractCode;

    // Thông tin phòng
    private Long roomId;
    private String roomCode;
    private String areaName;
    private String address;

    // Thông tin người thuê
    private Long tenantId;
    private String tenantName;
    private String tenantPhone;

    // Thời hạn hợp đồng
    private LocalDate startDate;
    private LocalDate endDate;

    /** Số ngày còn lại đến khi hết hạn (âm = đã hết hạn) */
    private Long daysUntilExpiry;

    // Tài chính
    private BigDecimal actualRentPrice;

    /**
     * Trạng thái: ACTIVE | EXPIRED | TERMINATED_EARLY
     */
    private String status;

    /**
     * Danh sách dịch vụ đã đăng ký kèm theo hợp đồng.
     * Người thuê xem để biết đang dùng những dịch vụ gì.
     */
    private List<ServiceDTO> registeredServices;

    /**
     * Điều khoản phạt nếu chấm dứt hợp đồng sớm.
     * Lưu dạng text tự do (vd: "Phạt 1 tháng tiền cọc nếu ra trước hạn 30 ngày").
     * Tương ứng cột penalty_terms trong bảng contracts (cần thêm vào DB nếu muốn dùng).
     */
    private String penaltyTerms;

    private Instant createdAt;

    // Thông tin tiền cọc liên kết
    private Long depositId;
    private BigDecimal depositAmount;
}