package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MeterReadingDTO: Dữ liệu nhập chỉ số điện nước từ chủ trọ (Mục 2.4).
 *
 * Luồng sử dụng:
 *   1. Đầu tháng: InvoiceScheduler tạo hóa đơn với elecOld/New = 0
 *   2. Chủ trọ đi đọc đồng hồ thực tế
 *   3. Chủ trọ gọi PUT /invoices/{id}/meters với DTO này
 *   4. BillingService tính lại total_amount và lưu vào DB
 *
 * Validation:
 *   - elecNew phải >= elecOld (chỉ số mới không được nhỏ hơn chỉ số cũ)
 *   - waterNew phải >= waterOld
 *   - Tất cả giá trị phải >= 0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeterReadingDTO {

    /**
     * Chỉ số điện kỳ trước (kWh).
     * Thường bằng elecNew của tháng trước, hệ thống có thể tự điền.
     */
    private Integer elecOld;

    /**
     * Chỉ số điện kỳ này (kWh).
     * Chủ trọ nhập sau khi đọc đồng hồ thực tế.
     */
    private Integer elecNew;

    /**
     * Chỉ số nước kỳ trước (m³).
     */
    private Integer waterOld;

    /**
     * Chỉ số nước kỳ này (m³).
     */
    private Integer waterNew;
}