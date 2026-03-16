package com.project.logiclayer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * EmailService: Gửi email thông báo song song với FCM push notification (Mục 2.8).
 *
 * Dùng Spring Boot Mail Starter + Gmail SMTP (hoặc SendGrid, Mailgun...).
 *
 * CẦU HÌNH TRONG application.properties:
 * ─────────────────────────────────────
 * spring.mail.host=smtp.gmail.com
 * spring.mail.port=587
 * spring.mail.username=your-email@gmail.com
 * spring.mail.password=your-app-password        ← Tạo App Password trong Google Account
 * spring.mail.properties.mail.smtp.auth=true
 * spring.mail.properties.mail.smtp.starttls.enable=true
 * app.mail.from=your-email@gmail.com
 * app.mail.enabled=true                         ← false để tắt email trong dev
 * ─────────────────────────────────────
 *
 * CẦN THÊM DEPENDENCY vào pom.xml của logic-layer:
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-mail</artifactId>
 * </dependency>
 */
@Service
public class EmailService {

    @Autowired(required = false) // required=false để app không crash nếu chưa cấu hình SMTP
    private JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@phongtro40.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean emailEnabled;

    // ─── Gửi email nhắc thanh toán ───────────────────────────────────────────

    /**
     * Gửi email nhắc thanh toán hóa đơn.
     * Được gọi từ NotificationScheduler song song với FCM.
     *
     * @param toEmail     Email người thuê
     * @param tenantName  Tên người thuê
     * @param invoiceCode Mã hóa đơn
     * @param amount      Số tiền cần thanh toán
     * @param daysLeft    Số ngày còn lại (âm = đã quá hạn)
     */
    public void sendPaymentReminder(String toEmail, String tenantName,
                                    String invoiceCode, double amount, int daysLeft) {
        if (!isReady(toEmail)) return;

        String subject;
        String body;

        if (daysLeft > 0) {
            subject = "[Phòng Trọ 4.0] Nhắc thanh toán hóa đơn " + invoiceCode;
            body = buildPaymentReminderBody(tenantName, invoiceCode, amount,
                    "Hóa đơn của bạn sẽ đến hạn trong " + daysLeft + " ngày.");
        } else if (daysLeft == 0) {
            subject = "[Phòng Trọ 4.0] Hóa đơn " + invoiceCode + " đến hạn hôm nay";
            body = buildPaymentReminderBody(tenantName, invoiceCode, amount,
                    "Hóa đơn của bạn đến hạn hôm nay. Vui lòng thanh toán ngay.");
        } else {
            subject = "[Phòng Trọ 4.0] KHẨN: Hóa đơn " + invoiceCode + " đã quá hạn";
            body = buildPaymentReminderBody(tenantName, invoiceCode, amount,
                    "Hóa đơn của bạn đã QUÁ HẠN " + Math.abs(daysLeft) + " ngày. "
                            + "Vui lòng thanh toán ngay để tránh phát sinh phí phạt.");
        }

        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Gửi email nhắc hợp đồng sắp hết hạn.
     */
    public void sendContractExpiryReminder(String toEmail, String tenantName,
                                           String contractCode, int daysLeft) {
        if (!isReady(toEmail)) return;

        String subject = "[Phòng Trọ 4.0] Hợp đồng " + contractCode + " còn " + daysLeft + " ngày";
        String body = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <h2 style="color: #e67e22;">Thông báo hợp đồng sắp hết hạn</h2>
              <p>Kính gửi <strong>%s</strong>,</p>
              <p>Hợp đồng thuê phòng <strong>%s</strong> của bạn sẽ hết hạn sau
              <strong>%d ngày</strong>.</p>
              <p>Nếu bạn muốn tiếp tục thuê, vui lòng liên hệ chủ trọ để gia hạn hợp đồng
              trước khi hết hạn.</p>
              <p>Trân trọng,<br/>Hệ thống Phòng Trọ 4.0</p>
            </div>
            """, tenantName, contractCode, daysLeft);

        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Gửi email thông báo khiếu nại đã được xử lý.
     */
    public void sendIssueResolved(String toEmail, String tenantName, String issueTitle) {
        if (!isReady(toEmail)) return;

        String subject = "[Phòng Trọ 4.0] Khiếu nại của bạn đã được xử lý";
        String body = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <h2 style="color: #27ae60;">Khiếu nại đã xử lý xong</h2>
              <p>Kính gửi <strong>%s</strong>,</p>
              <p>Khiếu nại "<strong>%s</strong>" của bạn đã được xử lý xong.</p>
              <p>Vui lòng đăng nhập vào ứng dụng để xác nhận và đánh giá kết quả xử lý.</p>
              <p>Trân trọng,<br/>Hệ thống Phòng Trọ 4.0</p>
            </div>
            """, tenantName, issueTitle);

        sendHtmlEmail(toEmail, subject, body);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private String buildPaymentReminderBody(String tenantName, String invoiceCode,
                                            double amount, String urgencyMessage) {
        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <h2 style="color: #e74c3c;">Nhắc nhở thanh toán hóa đơn</h2>
              <p>Kính gửi <strong>%s</strong>,</p>
              <p>%s</p>
              <table style="border-collapse: collapse; width: 100%%;">
                <tr style="background: #f8f9fa;">
                  <td style="padding: 8px; border: 1px solid #dee2e6;">Mã hóa đơn</td>
                  <td style="padding: 8px; border: 1px solid #dee2e6;"><strong>%s</strong></td>
                </tr>
                <tr>
                  <td style="padding: 8px; border: 1px solid #dee2e6;">Số tiền</td>
                  <td style="padding: 8px; border: 1px solid #dee2e6;">
                    <strong style="color: #e74c3c;">%,.0f VNĐ</strong>
                  </td>
                </tr>
              </table>
              <p style="margin-top: 16px;">Vui lòng thanh toán đúng hạn để duy trì dịch vụ.</p>
              <p>Trân trọng,<br/>Hệ thống Phòng Trọ 4.0</p>
            </div>
            """, tenantName, urgencyMessage, invoiceCode, amount);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log lỗi nhưng không ném exception — không để lỗi email làm fail business logic
            System.err.println("[EmailService] Gửi email thất bại đến " + to + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[EmailService] Lỗi không xác định khi gửi email: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra điều kiện trước khi gửi:
     * - emailEnabled = true trong config
     * - mailSender đã được inject (SMTP đã cấu hình)
     * - email đích không null/trống
     */
    private boolean isReady(String toEmail) {
        if (!emailEnabled) return false;
        if (mailSender == null) {
            System.err.println("[EmailService] JavaMailSender chưa được cấu hình. "
                    + "Thêm spring.mail.* vào application.properties.");
            return false;
        }
        if (toEmail == null || toEmail.isBlank()) return false;
        return true;
    }
}