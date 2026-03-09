package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * InvoiceSummaryDTO: Tổng hợp hóa đơn và bảng kê chi tiết dịch vụ.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceSummaryDTO {
    private Long invoiceId;
    private String invoiceCode;
    private Integer billingMonth;
    private Integer billingYear;

    private Integer elecOld;
    private Integer elecNew;
    private Integer waterOld;
    private Integer waterNew;

    private BigDecimal serviceFees; // Tổng tiền các dịch vụ cộng thêm
    private BigDecimal totalAmount; // Tổng tiền cuối cùng
    private String status;

    // Danh sách các dịch vụ chi tiết (từ bảng invoice_service_details)
    private List<InvoiceServiceDetailDTO> serviceDetails;
}