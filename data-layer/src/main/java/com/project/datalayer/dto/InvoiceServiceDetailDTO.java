package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * InvoiceServiceDetailDTO: Chi tiết từng dịch vụ trong hóa đơn tháng.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceServiceDetailDTO {
    private String serviceName;
    private BigDecimal servicePrice;
    private Integer quantity;
    private BigDecimal subTotal;
}