package com.project.logiclayer.scheduler;

import com.project.datalayer.entity.Contract;
import com.project.datalayer.entity.Invoice;
import com.project.datalayer.repository.ContractRepository;
import com.project.datalayer.repository.InvoiceRepository;
import com.project.logiclayer.service.EmailService;
import com.project.logiclayer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * NotificationScheduler (cập nhật): Gửi cả FCM push notification VÀ Email (Mục 2.8).
 *
 * THAY ĐỔI SO VỚI FILE GỐC:
 * - Inject thêm EmailService
 * - Mỗi chỗ gọi NotificationService cũng gọi thêm EmailService tương ứng
 * - Email dùng toEmail lấy từ user.getEmail() (có thể null → EmailService tự bỏ qua)
 *
 * Lịch chạy (giữ nguyên):
 * - 09:00 mỗi ngày → nhắc hóa đơn sắp đến hạn
 * - 09:30 mỗi ngày → nhắc hợp đồng sắp hết hạn + tự động EXPIRED
 */
@Component
public class NotificationScheduler {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ContractRepository contractRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailService emailService;

    // ─── Nhắc hóa đơn sắp đến hạn ────────────────────────────────────────────

    @Scheduled(cron = "0 0 9 * * ?")
    public void remindUpcomingPayments() {
        LocalDate today = LocalDate.now();
        int currentDay = today.getDayOfMonth();

        if (currentDay != 3 && currentDay != 7) return;
        int daysLeft = 10 - currentDay;

        List<Invoice> pendingInvoices = invoiceRepository
                .findByStatusAndBillingMonthAndBillingYear(
                        "UNPAID", today.getMonthValue(), today.getYear());

        for (Invoice invoice : pendingInvoices) {
            var tenant = invoice.getContract().getTenant();
            double amount = invoice.getTotalAmount() != null
                    ? invoice.getTotalAmount().doubleValue() : 0;

            // Gửi FCM push notification
            notificationService.sendPaymentReminder(
                    tenant.getId(), invoice.getInvoiceCode(), amount, daysLeft);

            // Gửi Email song song (bỏ qua nếu user không có email hoặc SMTP chưa cấu hình)
            if (tenant.getEmail() != null) {
                emailService.sendPaymentReminder(
                        tenant.getEmail(), tenant.getFullName(),
                        invoice.getInvoiceCode(), amount, daysLeft);
            }
        }

        System.out.println("[Scheduler] Đã gửi nhắc thanh toán: " + pendingInvoices.size() + " hóa đơn");
    }

    // ─── Nhắc hợp đồng sắp hết hạn ───────────────────────────────────────────

    @Scheduled(cron = "0 30 9 * * ?")
    public void remindExpiringContracts() {
        LocalDate today = LocalDate.now();
        List<Integer> reminderDays = List.of(30, 7);

        List<Contract> activeContracts = contractRepository.findByStatus("ACTIVE");

        for (Contract contract : activeContracts) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, contract.getEndDate());
            var tenant = contract.getTenant();

            if (reminderDays.contains((int) daysUntilExpiry)) {
                // FCM push
                notificationService.sendContractExpiryReminder(
                        tenant.getId(), contract.getContractCode(), (int) daysUntilExpiry);

                // Email song song
                if (tenant.getEmail() != null) {
                    emailService.sendContractExpiryReminder(
                            tenant.getEmail(), tenant.getFullName(),
                            contract.getContractCode(), (int) daysUntilExpiry);
                }
            }

            // Hợp đồng đã hết hạn → tự động cập nhật
            if (daysUntilExpiry < 0) {
                contract.setStatus("EXPIRED");
                contractRepository.save(contract);
                if (contract.getRoom() != null) {
                    contract.getRoom().setStatus("AVAILABLE");
                }
            }
        }
    }
}