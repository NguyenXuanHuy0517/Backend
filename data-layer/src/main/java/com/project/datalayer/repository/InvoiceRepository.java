package com.project.datalayer.repository;

import com.project.datalayer.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * InvoiceRepository — Fix đủ method.
 *
 * LỖI ĐÃ SỬA:
 * "cannot find symbol method findByTenantId(Long)"                             — BillingService :57
 * "cannot find symbol method findByStatus(String)"                             — BillingService :192, :193
 * "cannot find symbol method findPaidInvoicesByYear(Integer)"                  — ReportService :80
 * "cannot find symbol method getTotalAmount()"                                 — ReportService :92
 * "cannot find symbol method sumPaidAmountByMonthAndYear(int, Integer)"        — ReportService :104
 * "cannot find symbol method findByStatusAndBillingMonthAndBillingYear(...)"   — NotificationScheduler :48
 *
 * Lỗi getTotalAmount() :92 thực ra là Invoice entity chưa có getter getTotalAmount().
 * Nếu entity dùng Lombok @Data thì getter tự sinh. Nếu không, xem Invoice entity fix bên dưới.
 *
 * Cần copy file này vào:
 *   data-layer/src/main/java/com/project/datalayer/repository/InvoiceRepository.java
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Kiểm tra hóa đơn đã tồn tại cho tháng/năm của một hợp đồng chưa.
     * InvoiceScheduler dùng để tránh tạo hóa đơn trùng.
     */
    Optional<Invoice> findByContractIdAndBillingMonthAndBillingYear(
            Long contractId, Integer billingMonth, Integer billingYear);

    /**
     * Lấy tất cả hóa đơn của một người thuê (qua contract.tenant.id), mới nhất trước.
     * BillingService.getInvoicesByTenant() dùng method này.
     */
    @Query("SELECT i FROM Invoice i WHERE i.contract.tenant.id = :tenantId " +
            "ORDER BY i.billingYear DESC, i.billingMonth DESC")
    List<Invoice> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Lấy hóa đơn theo trạng thái (UNPAID / PAID / OVERDUE).
     * BillingService.getDebtList() và ReportService dùng method này.
     */
    List<Invoice> findByStatus(String status);

    /**
     * Lấy hóa đơn UNPAID của một tháng/năm cụ thể.
     * NotificationScheduler.remindUpcomingPayments() dùng để gửi nhắc hạn.
     */
    List<Invoice> findByStatusAndBillingMonthAndBillingYear(
            String status, Integer billingMonth, Integer billingYear);

    /**
     * Lấy tất cả hóa đơn PAID trong một năm.
     * ReportService.getTotalRevenueByYear() dùng để tính tổng doanh thu năm.
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PAID' AND i.billingYear = :year")
    List<Invoice> findPaidInvoicesByYear(@Param("year") Integer year);

    /**
     * Tổng tiền đã thu trong một tháng/năm cụ thể.
     * ReportService.getRevenueByMonth() dùng để vẽ biểu đồ doanh thu theo tháng.
     * COALESCE trả về 0 thay vì null khi không có hóa đơn nào.
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
            "WHERE i.status = 'PAID' AND i.billingMonth = :month AND i.billingYear = :year")
    BigDecimal sumPaidAmountByMonthAndYear(
            @Param("month") Integer month, @Param("year") Integer year);
}