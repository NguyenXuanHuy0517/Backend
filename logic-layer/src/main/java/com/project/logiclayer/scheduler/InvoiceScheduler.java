package com.project.logiclayer.scheduler;

import com.project.datalayer.entity.Contract;
import com.project.datalayer.entity.Invoice;
import com.project.datalayer.repository.ContractRepository;
import com.project.datalayer.repository.InvoiceRepository;
import com.project.logiclayer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * InvoiceScheduler: Tự động hóa quy trình tạo hóa đơn và nhắc hạn (Mục 4).
 *
 * Hai nhiệm vụ chính:
 * 1. Đầu mỗi tháng: Tạo hóa đơn mới cho tất cả hợp đồng đang hoạt động
 * 2. Hàng ngày: Kiểm tra hóa đơn quá hạn và gửi thông báo nhắc
 *
 * Cron expression format: "giây phút giờ ngày tháng thứ"
 * - "0 0 1 1 * ?" → 01:00 ngày 1 hàng tháng (tạo hóa đơn)
 * - "0 0 8 * * ?" → 08:00 mỗi ngày (kiểm tra quá hạn)
 *
 * Lưu ý: Cần thêm @EnableScheduling vào LogicLayerApplication hoặc
 * một @Configuration class để kích hoạt scheduler.
 */
@Component
@EnableScheduling
public class InvoiceScheduler {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Tự động tạo hóa đơn vào lúc 01:00 ngày 1 mỗi tháng.
     *
     * Quy trình:
     * 1. Lấy tất cả hợp đồng đang ACTIVE và chưa hết hạn
     * 2. Với mỗi hợp đồng, kiểm tra xem đã có hóa đơn tháng này chưa
     * 3. Nếu chưa → tạo hóa đơn mới với status UNPAID
     * 4. Chỉ số điện nước = 0 (chủ trọ sẽ nhập thực tế sau)
     *
     * Ghi chú: Tổng tiền (total_amount) sẽ được tính sau khi chủ trọ
     * nhập chỉ số điện nước thực tế qua BillingService.calculateMonthlyBill().
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    @Transactional
    public void createMonthlyInvoices() {
        System.out.println("[Scheduler] Bắt đầu tạo hóa đơn tháng: " + LocalDate.now());

        YearMonth currentMonth = YearMonth.now();
        int month = currentMonth.getMonthValue();
        int year = currentMonth.getYear();

        // Lấy tất cả hợp đồng đang hoạt động
        List<Contract> activeContracts = contractRepository.findAll().stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .filter(c -> !c.getEndDate().isBefore(LocalDate.now())) // Chưa hết hạn
                .toList();

        int created = 0;
        for (Contract contract : activeContracts) {
            // Kiểm tra đã có hóa đơn tháng này cho hợp đồng này chưa
            boolean alreadyExists = invoiceRepository.findAll().stream()
                    .anyMatch(inv ->
                            inv.getContract().getId().equals(contract.getId())
                            && inv.getBillingMonth() == month
                            && inv.getBillingYear() == year);

            if (!alreadyExists) {
                Invoice invoice = new Invoice();
                invoice.setContract(contract);
                invoice.setInvoiceCode("HD-" + contract.getContractCode() + "-" + year + month);
                invoice.setBillingMonth(month);
                invoice.setBillingYear(year);
                // Chỉ số ban đầu = 0, chủ trọ sẽ cập nhật
                invoice.setElecOld(0);
                invoice.setElecNew(0);
                invoice.setWaterOld(0);
                invoice.setWaterNew(0);
                invoice.setServiceFees(BigDecimal.ZERO);
                invoice.setStatus("UNPAID");
                invoiceRepository.save(invoice);
                created++;
            }
        }

        System.out.println("[Scheduler] Đã tạo " + created + " hóa đơn mới tháng " + month + "/" + year);
    }

    /**
     * Kiểm tra và nhắc hóa đơn quá hạn vào lúc 08:00 mỗi ngày.
     *
     * Logic nhắc nhở:
     * - Hóa đơn tháng trước mà chưa thanh toán = quá hạn
     * - Gửi notification cho người thuê
     * - Cập nhật status sang OVERDUE
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkOverdueInvoices() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);

        List<Invoice> overdueInvoices = invoiceRepository.findAll().stream()
                .filter(inv -> "UNPAID".equals(inv.getStatus()))
                .filter(inv -> {
                    // Hóa đơn của tháng trước trở về trước mà chưa trả = quá hạn
                    YearMonth invoiceMonth = YearMonth.of(inv.getBillingYear(), inv.getBillingMonth());
                    return invoiceMonth.isBefore(YearMonth.now());
                })
                .toList();

        for (Invoice invoice : overdueInvoices) {
            // Cập nhật trạng thái
            invoice.setStatus("OVERDUE");
            invoiceRepository.save(invoice);

            // Gửi thông báo cho người thuê
            Long tenantId = invoice.getContract().getTenant().getId();
            double amount = invoice.getTotalAmount() != null
                    ? invoice.getTotalAmount().doubleValue() : 0;

            notificationService.sendPaymentReminder(tenantId, invoice.getInvoiceCode(), amount, -1);
        }

        if (!overdueInvoices.isEmpty()) {
            System.out.println("[Scheduler] Đã đánh dấu " + overdueInvoices.size() + " hóa đơn quá hạn");
        }
    }
}
