package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ReportDTO: Tổng hợp tất cả dữ liệu thống kê cho Dashboard của chủ trọ (Mục 2.11).
 *
 * Được dùng trong:
 *   GET /api/business/reports/dashboard   → Tổng quan toàn bộ
 *   GET /api/business/reports/revenue     → Doanh thu chi tiết
 *   GET /api/business/reports/occupancy   → Tỷ lệ lấp đầy
 *   GET /api/business/reports/services    → Dịch vụ phổ biến
 *   GET /api/business/reports/issues      → Thống kê khiếu nại
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {

    // ===== 2.11.1 Thống kê doanh thu =====

    /** Tổng doanh thu trong khoảng thời gian truy vấn */
    private BigDecimal totalRevenue;

    /** Tổng số hóa đơn đã thanh toán */
    private Integer totalPaidInvoices;

    /** Tổng số hóa đơn chưa thanh toán */
    private Integer totalUnpaidInvoices;

    /** Tổng số hóa đơn quá hạn */
    private Integer totalOverdueInvoices;

    /** Tổng số tiền đang nợ (chưa thu được) */
    private BigDecimal totalDebtAmount;

    /**
     * Doanh thu theo từng tháng trong năm.
     * Key: "2024-01", "2024-02", ... Value: tổng doanh thu tháng đó
     */
    private Map<String, BigDecimal> revenueByMonth;

    // ===== 2.11.2 Tỷ lệ lấp đầy =====

    /** Tổng số phòng toàn hệ thống */
    private Integer totalRooms;

    /** Số phòng đang được thuê */
    private Integer rentedRooms;

    /** Số phòng đang trống */
    private Integer availableRooms;

    /** Số phòng đang bảo trì */
    private Integer maintenanceRooms;

    /** Tỷ lệ lấp đầy (%) = rentedRooms / totalRooms * 100 */
    private Double occupancyRate;

    /**
     * Tỷ lệ lấp đầy theo từng khu trọ.
     * Key: tên khu trọ, Value: tỷ lệ % lấp đầy
     */
    private Map<String, Double> occupancyByArea;

    // ===== 2.11.3 Thống kê dịch vụ =====

    /**
     * Top dịch vụ được đăng ký nhiều nhất.
     * Mỗi phần tử: { serviceName, count, totalRevenue }
     */
    private List<ServiceUsageDTO> topServices;

    // ===== 2.11.4 Thống kê khiếu nại =====

    /** Tổng số khiếu nại trong kỳ */
    private Integer totalIssues;

    /** Số khiếu nại đang mở / đang xử lý */
    private Integer openIssues;

    /** Số khiếu nại đã hoàn thành */
    private Integer resolvedIssues;

    /**
     * Phân loại khiếu nại theo mức ưu tiên.
     * Key: "LOW"/"MEDIUM"/"HIGH"/"URGENT", Value: số lượng
     */
    private Map<String, Integer> issuesByPriority;

    // ===== Inner DTO =====

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceUsageDTO {
        private Long serviceId;
        private String serviceName;
        private Integer contractCount;      // Số hợp đồng đang dùng dịch vụ này
        private BigDecimal monthlyRevenue;  // Doanh thu tháng từ dịch vụ này
    }
}