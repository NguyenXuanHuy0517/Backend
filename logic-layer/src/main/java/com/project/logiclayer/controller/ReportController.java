package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.ReportDTO;
import com.project.logiclayer.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ReportController: API Thống kê và Báo cáo (Mục 2.11).
 * Chỉ HOST mới được truy cập các endpoint này.
 *
 * Endpoint:
 *   GET /api/business/reports/dashboard          → Tổng hợp tất cả (1 lần gọi)
 *   GET /api/business/reports/revenue?year=2024  → Doanh thu chi tiết
 *   GET /api/business/reports/occupancy          → Tỷ lệ lấp đầy
 *   GET /api/business/reports/services           → Dịch vụ phổ biến
 *   GET /api/business/reports/issues             → Phân tích khiếu nại
 */
@RestController
@RequestMapping("/api/business/reports")
// @PreAuthorize("hasRole('HOST')")   // Bỏ comment khi bật Spring Security
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Màn hình Dashboard chủ trọ — trả về toàn bộ thống kê trong 1 lần gọi.
     * Flutter dùng endpoint này để render trang Dashboard với nhiều widget.
     *
     * @param year Năm muốn xem (mặc định năm hiện tại nếu không truyền)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ReportDTO>> getDashboard(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(
                ApiResponse.success(reportService.getDashboard(year)));
    }

    /**
     * Thống kê doanh thu chi tiết.
     * Trả về: tổng thu, tổng nợ, doanh thu từng tháng trong năm.
     *
     * @param year Năm cần xem (mặc định năm hiện tại)
     */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenue(
            @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();

        Map<String, Object> data = Map.of(
                "year", year,
                "totalRevenue", reportService.getTotalRevenueByYear(year),
                "totalDebt", reportService.getTotalDebtAmount(),
                "revenueByMonth", reportService.getRevenueByMonth(year),
                "paidCount", reportService.getDashboard(year).getTotalPaidInvoices(),
                "unpaidCount", reportService.getDashboard(year).getTotalUnpaidInvoices(),
                "overdueCount", reportService.getDashboard(year).getTotalOverdueInvoices()
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Thống kê tỷ lệ lấp đầy phòng.
     * Trả về: tổng phòng, số phòng theo trạng thái, tỷ lệ % lấp đầy, breakdown theo khu.
     */
    @GetMapping("/occupancy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOccupancy() {
        Map<String, Object> data = Map.of(
                "totalRooms", reportService.getTotalRooms(),
                "rentedRooms", reportService.getRoomCountByStatus("RENTED"),
                "availableRooms", reportService.getRoomCountByStatus("AVAILABLE"),
                "maintenanceRooms", reportService.getRoomCountByStatus("MAINTENANCE"),
                "occupancyRate", reportService.getOccupancyRate(),
                "occupancyByArea", reportService.getOccupancyByArea()
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Thống kê dịch vụ được sử dụng.
     * Trả về top 10 dịch vụ được đăng ký nhiều nhất và doanh thu tương ứng.
     */
    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ReportDTO.ServiceUsageDTO>>> getServiceStats() {
        return ResponseEntity.ok(
                ApiResponse.success(reportService.getTopServices()));
    }

    /**
     * Phân tích khiếu nại và bảo trì.
     * Trả về: tổng số, theo trạng thái, theo mức độ ưu tiên.
     */
    @GetMapping("/issues")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIssueStats() {
        ReportDTO report = reportService.getDashboard(null);
        Map<String, Object> data = Map.of(
                "totalIssues", report.getTotalIssues(),
                "openIssues", report.getOpenIssues(),
                "resolvedIssues", report.getResolvedIssues(),
                "issuesByPriority", report.getIssuesByPriority()
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}