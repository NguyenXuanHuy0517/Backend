package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TenantDTO: Thông tin người thuê dành cho Host quản lý.
 * Bao gồm thông tin cá nhân + hợp đồng/phòng hiện tại (nếu có).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantDTO {
    // ── Thông tin cá nhân ──
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String idCardNumber;
    private Boolean isActive;

    // ── Hợp đồng hiện tại (null nếu chưa có) ──
    private Long contractId;
    private String contractCode;
    private String contractStatus;   // ACTIVE | EXPIRED | TERMINATED
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal actualRentPrice;

    // ── Phòng đang thuê (null nếu chưa có) ──
    private Long roomId;
    private String roomCode;
    private String areaName;
}