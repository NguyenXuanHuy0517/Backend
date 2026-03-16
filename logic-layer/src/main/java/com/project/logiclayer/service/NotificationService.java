package com.project.logiclayer.service;

import com.project.datalayer.entity.User;
import com.project.datalayer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * NotificationService: Gửi push notification đến Flutter App qua Firebase (FCM).
 *
 * Cách FCM hoạt động:
 * 1. Khi Flutter App khởi động, lấy FCM token từ Firebase SDK
 * 2. Gửi token này lên server và lưu vào cột users.fcm_token
 * 3. Khi server muốn thông báo, gửi HTTP POST đến FCM API kèm token
 * 4. Firebase chuyển tiếp notification đến thiết bị của người dùng
 *
 * Cần cấu hình trong application.properties:
 *   fcm.server-key=YOUR_FIREBASE_SERVER_KEY
 *   (Lấy từ Firebase Console → Project Settings → Cloud Messaging)
 *
 * Trong production nên dùng Firebase Admin SDK thay vì gọi HTTP trực tiếp.
 */
@Service
public class NotificationService {

    @Value("${fcm.server-key:YOUR_FCM_SERVER_KEY_HERE}")
    private String fcmServerKey;

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    @Autowired
    private UserRepository userRepository;

    /**
     * Gửi notification đến một người dùng cụ thể qua FCM token.
     *
     * @param userId  ID người nhận
     * @param title   Tiêu đề notification (hiển thị đậm)
     * @param body    Nội dung notification
     */
    public void sendToUser(Long userId, String title, String body) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
                sendFcmNotification(user.getFcmToken(), title, body);
            }
            // Nếu không có FCM token (user chưa đăng nhập thiết bị) → bỏ qua
        });
    }

    /**
     * Gửi notification nhắc thanh toán hóa đơn.
     * Được gọi từ NotificationScheduler.
     */
    public void sendPaymentReminder(Long tenantId, String invoiceCode, double amount, int daysLeft) {
        String title = "Nhắc nhở thanh toán";
        String body;

        if (daysLeft > 0) {
            body = String.format(
                "Hóa đơn %s (%.0f VNĐ) sẽ đến hạn trong %d ngày. Vui lòng thanh toán đúng hạn.",
                invoiceCode, amount, daysLeft);
        } else if (daysLeft == 0) {
            body = String.format(
                "Hóa đơn %s (%.0f VNĐ) đến hạn hôm nay. Vui lòng thanh toán ngay.",
                invoiceCode, amount);
        } else {
            title = "Hóa đơn quá hạn";
            body = String.format(
                "Hóa đơn %s (%.0f VNĐ) đã quá hạn %d ngày. Vui lòng thanh toán ngay để tránh phí phạt.",
                invoiceCode, amount, Math.abs(daysLeft));
        }

        sendToUser(tenantId, title, body);
    }

    /**
     * Gửi notification nhắc sắp hết hạn hợp đồng.
     */
    public void sendContractExpiryReminder(Long tenantId, String contractCode, int daysLeft) {
        String title = "Hợp đồng sắp hết hạn";
        String body = String.format(
            "Hợp đồng %s của bạn còn %d ngày nữa sẽ hết hạn. " +
            "Vui lòng liên hệ chủ trọ để gia hạn nếu có nhu cầu.",
            contractCode, daysLeft);

        sendToUser(tenantId, title, body);
    }

    /**
     * Gọi FCM API để gửi notification thực sự đến thiết bị.
     */
    private void sendFcmNotification(String fcmToken, String title, String body) {
        try {
            // Tạo JSON payload theo định dạng FCM Legacy API
            String payload = String.format("""
                {
                  "to": "%s",
                  "notification": {
                    "title": "%s",
                    "body": "%s",
                    "sound": "default"
                  },
                  "data": {
                    "click_action": "FLUTTER_NOTIFICATION_CLICK"
                  }
                }
                """, fcmToken, title, body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FCM_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "key=" + fcmServerKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Log kết quả (trong production dùng proper logging)
            if (response.statusCode() != 200) {
                System.err.println("FCM gửi thất bại: " + response.body());
            }

        } catch (Exception e) {
            // Không để lỗi FCM làm fail business logic chính
            System.err.println("Lỗi gửi FCM notification: " + e.getMessage());
        }
    }
}
