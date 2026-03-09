package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ContractRequestDTO: Dữ liệu gửi lên từ App để tạo hợp đồng mới.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractRequestDTO {
    private String contractCode;
    private Long roomId;
    private Long tenantId;
    private Long depositId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal actualRentPrice;
    private List<Long> serviceIds; // Danh sách các dịch vụ khách chọn đăng ký
}
