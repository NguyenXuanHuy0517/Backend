package com.project.logiclayer.service;

import com.project.datalayer.dto.ReportDTO;
import com.project.datalayer.entity.Room;
import com.project.datalayer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReportService: Xử lý toàn bộ nghiệp vụ Thống kê và Báo cáo (Mục 2.11).
 *
 * 4 nhóm thống kê chính:
 *   2.11.1 - Doanh thu theo thời gian
 *   2.11.2 - Tỷ lệ lấp đầy phòng
 *   2.11.3 - Thống kê dịch vụ được sử dụng
 *   2.11.4 - Thống kê số lượng và loại khiếu nại
 *
 * Ngoài ra có getDashboard() trả về tổng hợp tất cả để Flutter
 * hiển thị màn hình Dashboard chính của chủ trọ trong một lần gọi API.
 */
@Service
public class ReportService {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private IssueRepository issueRepository;
    @Autowired private ServiceRepository serviceRepository;

    // ─── Dashboard tổng hợp ───────────────────────────────────────────────────

    /**
     * Trả về toàn bộ dữ liệu thống kê trong một lần gọi.
     * Flutter dùng endpoint này để render màn hình Dashboard chủ trọ.
     *
     * @param year Năm cần thống kê (mặc định năm hiện tại)
     */
    public ReportDTO getDashboard(Integer year) {
        if (year == null) year = LocalDate.now().getYear();

        return ReportDTO.builder()
                // 2.11.1 Doanh thu
                .totalRevenue(getTotalRevenueByYear(year))
                .totalPaidInvoices(invoiceRepository.findByStatus("PAID").size())
                .totalUnpaidInvoices(invoiceRepository.findByStatus("UNPAID").size())
                .totalOverdueInvoices(invoiceRepository.findByStatus("OVERDUE").size())
                .totalDebtAmount(getTotalDebtAmount())
                .revenueByMonth(getRevenueByMonth(year))
                // 2.11.2 Lấp đầy
                .totalRooms(getTotalRooms())
                .rentedRooms(getRoomCountByStatus("RENTED"))
                .availableRooms(getRoomCountByStatus("AVAILABLE"))
                .maintenanceRooms(getRoomCountByStatus("MAINTENANCE"))
                .occupancyRate(getOccupancyRate())
                .occupancyByArea(getOccupancyByArea())
                // 2.11.3 Dịch vụ
                .topServices(getTopServices())
                // 2.11.4 Khiếu nại
                .totalIssues((int) issueRepository.count())
                .openIssues((int) (issueRepository.countByStatus("OPEN")
                        + issueRepository.countByStatus("PROCESSING")))
                .resolvedIssues((int) (issueRepository.countByStatus("RESOLVED")
                        + issueRepository.countByStatus("CLOSED")))
                .issuesByPriority(getIssuesByPriority())
                .build();
    }

    // ─── 2.11.1 Thống kê doanh thu ────────────────────────────────────────────

    /**
     * Tổng doanh thu đã thu trong một năm (chỉ tính hóa đơn PAID).
     */
    public BigDecimal getTotalRevenueByYear(Integer year) {
        return invoiceRepository.findPaidInvoicesByYear(year).stream()
                .map(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tổng tiền nợ chưa thu (UNPAID + OVERDUE).
     */
    public BigDecimal getTotalDebtAmount() {
        List<String> debtStatuses = List.of("UNPAID", "OVERDUE");
        return debtStatuses.stream()
                .flatMap(status -> invoiceRepository.findByStatus(status).stream())
                .map(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Doanh thu từng tháng trong năm.
     * Trả về Map với key "2024-01", "2024-02"... và value là tổng doanh thu.
     */
    public Map<String, BigDecimal> getRevenueByMonth(Integer year) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            String key = year + "-" + String.format("%02d", month);
            BigDecimal revenue = invoiceRepository.sumPaidAmountByMonthAndYear(month, year);
            result.put(key, revenue != null ? revenue : BigDecimal.ZERO);
        }
        return result;
    }

    // ─── 2.11.2 Tỷ lệ lấp đầy ────────────────────────────────────────────────

    public int getTotalRooms() {
        return (int) roomRepository.count();
    }

    public int getRoomCountByStatus(String status) {
        return (int) roomRepository.findByStatus(status).size();
    }

    /**
     * Tỷ lệ lấp đầy tổng thể = số phòng đang thuê / tổng số phòng × 100.
     * Trả về 0.0 nếu không có phòng nào.
     */
    public Double getOccupancyRate() {
        long total = roomRepository.count();
        if (total == 0) return 0.0;
        long rented = roomRepository.findByStatus("RENTED").size();
        return Math.round((double) rented / total * 100 * 10.0) / 10.0; // làm tròn 1 chữ số thập phân
    }

    /**
     * Tỷ lệ lấp đầy theo từng khu trọ.
     * Key: tên khu trọ, Value: % lấp đầy
     */
    public Map<String, Double> getOccupancyByArea() {
        Map<String, Double> result = new LinkedHashMap<>();
        areaRepository.findAll().forEach(area -> {
            List<Room> rooms = roomRepository.findByAreaId(area.getId());
            if (rooms.isEmpty()) {
                result.put(area.getAreaName(), 0.0);
                return;
            }
            long rented = rooms.stream()
                    .filter(r -> "RENTED".equalsIgnoreCase(r.getStatus()))
                    .count();
            double rate = Math.round((double) rented / rooms.size() * 100 * 10.0) / 10.0;
            result.put(area.getAreaName(), rate);
        });
        return result;
    }

    // ─── 2.11.3 Thống kê dịch vụ ─────────────────────────────────────────────

    /**
     * Top dịch vụ được đăng ký nhiều nhất.
     * Đếm số hợp đồng ACTIVE đang dùng mỗi dịch vụ và doanh thu tháng.
     */
    public List<ReportDTO.ServiceUsageDTO> getTopServices() {
        return serviceRepository.findAll().stream()
                .map(svc -> {
                    // Đếm số hợp đồng đang dùng dịch vụ này
                    int count = svc.getContractServices() != null
                            ? (int) svc.getContractServices().stream()
                            .filter(cs -> cs.getContract() != null
                                    && "ACTIVE".equals(cs.getContract().getStatus()))
                            .count()
                            : 0;

                    // Doanh thu tháng = đơn giá × số hợp đồng đang dùng
                    BigDecimal monthlyRevenue = svc.getPrice()
                            .multiply(BigDecimal.valueOf(count));

                    return ReportDTO.ServiceUsageDTO.builder()
                            .serviceId(svc.getId())
                            .serviceName(svc.getServiceName())
                            .contractCount(count)
                            .monthlyRevenue(monthlyRevenue)
                            .build();
                })
                // Sắp xếp theo số hợp đồng đang dùng, nhiều nhất lên đầu
                .sorted(Comparator.comparingInt(ReportDTO.ServiceUsageDTO::getContractCount).reversed())
                .limit(10) // Chỉ lấy top 10
                .collect(Collectors.toList());
    }

    // ─── 2.11.4 Thống kê khiếu nại ───────────────────────────────────────────

    /**
     * Số lượng khiếu nại theo mức ưu tiên.
     * Key: LOW / MEDIUM / HIGH / URGENT, Value: số lượng
     */
    public Map<String, Integer> getIssuesByPriority() {
        Map<String, Integer> result = new LinkedHashMap<>();
        List.of("LOW", "MEDIUM", "HIGH", "URGENT").forEach(priority ->
                result.put(priority, (int) issueRepository.countByPriority(priority)));
        return result;
    }
}