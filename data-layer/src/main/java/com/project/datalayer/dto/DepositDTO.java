package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DepositDTO: Thông tin cọc tiền giữa chủ trọ và người thuê (Mục 2.6).
 *
 * Vòng đời của Deposit:
 *   PENDING    → Người thuê đặt cọc, chờ chủ trọ xác nhận
 *   CONFIRMED  → Chủ trọ đã nhận cọc, chờ người thuê check-in
 *   COMPLETED  → Đã tạo hợp đồng, cọc được tính vào hợp đồng
 *   EXPIRED    → Quá ngày check-in mà không đến
 *   CANCELLED_NO_REFUND → Hủy, mất cọc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositDTO {

    private Long depositId;

    // Thông tin người thuê đặt cọc
    private Long tenantId;
    private String tenantName;
    private String tenantPhone;

    // Thông tin phòng được đặt cọc
    private Long roomId;
    private String roomCode;
    private String areaName;

    private BigDecimal amount;       // Số tiền cọc
    private Instant depositDate;     // Ngày đặt cọc
    private LocalDate expectedCheckIn; // Ngày dự kiến vào ở

    /**
     * Trạng thái cọc: PENDING | CONFIRMED | COMPLETED | EXPIRED | CANCELLED_NO_REFUND
     * Khớp với ENUM trong bảng deposits.
     */
    private String status;
}