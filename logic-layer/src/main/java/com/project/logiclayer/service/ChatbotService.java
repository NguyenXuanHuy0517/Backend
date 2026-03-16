package com.project.logiclayer.service;

import com.project.datalayer.entity.ChatbotHistory;
import com.project.datalayer.entity.User;
import com.project.datalayer.repository.ChatbotRepository;
import com.project.datalayer.repository.InvoiceRepository;
import com.project.datalayer.repository.UserRepository;
import com.project.logiclayer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ChatbotService: Chatbot dựa trên luật (Rule-based) theo Mục 2.10.
 *
 * Cách hoạt động:
 * 1. Nhận câu hỏi từ người dùng (String)
 * 2. So khớp với các từ khóa/pattern đã định nghĩa → xác định "intent"
 * 3. Dựa vào intent, truy vấn DB hoặc trả lời cố định
 * 4. Lưu lịch sử vào bảng chatbot_history
 *
 * Để mở rộng: có thể tích hợp NLP library (OpenNLP, VnCoreNLP) để
 * nhận diện intent tốt hơn, hoặc gọi API AI ngoài.
 */
@Service
public class ChatbotService {

    @Autowired
    private ChatbotRepository chatbotRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    // Mapping từ khóa → intent
    // Thứ tự quan trọng: từ khóa cụ thể nên check trước
    private static final Map<String[], String> INTENT_RULES = Map.of(
        new String[]{"tiền phòng", "giá phòng", "tiền thuê", "bao nhiêu tiền"}, "inquiry_rent_price",
        new String[]{"hóa đơn", "hóa đơn tháng", "thanh toán", "nợ"}, "inquiry_invoice",
        new String[]{"điện", "nước", "chỉ số"}, "inquiry_utilities",
        new String[]{"hợp đồng", "hết hạn", "gia hạn"}, "inquiry_contract",
        new String[]{"dịch vụ", "wifi", "vệ sinh", "máy giặt"}, "inquiry_service",
        new String[]{"khiếu nại", "báo hỏng", "sửa chữa", "bảo trì"}, "guide_issue",
        new String[]{"xin chào", "hello", "hi", "chào"}, "greeting",
        new String[]{"cảm ơn", "thanks", "tạm biệt"}, "farewell"
    );

    /**
     * Xử lý câu hỏi của người dùng và trả về câu trả lời.
     *
     * @param question Câu hỏi từ người dùng
     * @param userId   ID người dùng (để lưu lịch sử và truy vấn dữ liệu cá nhân)
     * @return Câu trả lời của chatbot
     */
    @Transactional
    public String processQuestion(String question, Long userId) {

        String lowerQuestion = question.toLowerCase().trim();
        String intent = detectIntent(lowerQuestion);
        String answer = generateAnswer(intent, userId, lowerQuestion);

        // Lưu lịch sử chat vào DB
        saveHistory(userId, question, answer, intent);

        return answer;
    }

    /**
     * Xác định intent từ câu hỏi bằng cách so khớp từ khóa.
     */
    private String detectIntent(String question) {
        for (Map.Entry<String[], String> rule : INTENT_RULES.entrySet()) {
            for (String keyword : rule.getKey()) {
                if (question.contains(keyword)) {
                    return rule.getValue();
                }
            }
        }
        return "unknown";
    }

    /**
     * Sinh câu trả lời dựa trên intent.
     * Với các intent cần dữ liệu thực, truy vấn DB và điền vào template.
     */
    private String generateAnswer(String intent, Long userId, String question) {
        return switch (intent) {
            case "greeting" ->
                "Xin chào! Tôi là trợ lý ảo của hệ thống Phòng Trọ 4.0. " +
                "Bạn có thể hỏi tôi về hóa đơn, hợp đồng, dịch vụ hoặc cách sử dụng hệ thống.";

            case "farewell" ->
                "Cảm ơn bạn đã sử dụng dịch vụ. Chúc bạn một ngày tốt lành!";

            case "inquiry_invoice" -> getInvoiceInfo(userId);

            case "inquiry_rent_price" ->
                "Để xem giá phòng của bạn, vui lòng vào mục " +
                "'Hợp đồng của tôi' → 'Chi tiết hợp đồng'. " +
                "Bạn cũng có thể hỏi 'hóa đơn tháng này' để xem chi tiết các khoản phí.";

            case "inquiry_utilities" ->
                "Chỉ số điện nước được cập nhật hàng tháng bởi chủ trọ. " +
                "Bạn có thể xem chi tiết trong hóa đơn tháng. " +
                "Nếu thấy số liệu không đúng, hãy liên hệ chủ trọ hoặc gửi khiếu nại.";

            case "inquiry_contract" ->
                "Thông tin hợp đồng của bạn có trong mục 'Hợp đồng của tôi'. " +
                "Hệ thống sẽ tự động thông báo trước 30 ngày khi hợp đồng sắp hết hạn. " +
                "Để gia hạn, liên hệ trực tiếp với chủ trọ.";

            case "inquiry_service" ->
                "Danh sách dịch vụ có trong mục 'Dịch vụ đã đăng ký'. " +
                "Để đăng ký thêm hoặc hủy dịch vụ, vui lòng liên hệ chủ trọ " +
                "hoặc gửi yêu cầu qua mục 'Khiếu nại & Yêu cầu'.";

            case "guide_issue" ->
                "Để báo cáo sự cố, bạn làm theo các bước sau:\n" +
                "1. Vào mục 'Khiếu nại & Bảo trì'\n" +
                "2. Nhấn nút 'Tạo khiếu nại mới'\n" +
                "3. Điền tiêu đề, mô tả và đính kèm ảnh (nếu có)\n" +
                "4. Chọn mức độ ưu tiên và gửi\n" +
                "Chủ trọ sẽ tiếp nhận và xử lý trong thời gian sớm nhất.";

            default ->
                "Xin lỗi, tôi chưa hiểu câu hỏi của bạn. " +
                "Bạn có thể hỏi về: hóa đơn, tiền phòng, hợp đồng, dịch vụ, " +
                "chỉ số điện nước, hoặc cách báo sự cố bảo trì.";
        };
    }

    /**
     * Lấy thông tin hóa đơn gần nhất của người dùng.
     */
    private String getInvoiceInfo(Long userId) {
        try {
            // Tìm hóa đơn chưa thanh toán của user thông qua contract
            var unpaidInvoices = invoiceRepository.findAll().stream()
                    .filter(inv -> inv.getContract() != null
                            && inv.getContract().getTenant() != null
                            && inv.getContract().getTenant().getId().equals(userId)
                            && "UNPAID".equals(inv.getStatus()))
                    .toList();

            if (unpaidInvoices.isEmpty()) {
                return "Bạn hiện không có hóa đơn nào chưa thanh toán. " +
                       "Để xem lịch sử hóa đơn, vào mục 'Hóa đơn của tôi'.";
            }

            var latest = unpaidInvoices.get(0);
            return String.format(
                "Hóa đơn tháng %d/%d của bạn:\n" +
                "- Mã hóa đơn: %s\n" +
                "- Tổng tiền: %,.0f VNĐ\n" +
                "- Trạng thái: Chưa thanh toán\n" +
                "Vui lòng thanh toán đúng hạn để tránh phát sinh phí phạt.",
                latest.getBillingMonth(),
                latest.getBillingYear(),
                latest.getInvoiceCode(),
                latest.getTotalAmount() != null ? latest.getTotalAmount().doubleValue() : 0
            );
        } catch (Exception e) {
            return "Không thể lấy thông tin hóa đơn lúc này. Vui lòng thử lại sau.";
        }
    }

    private void saveHistory(Long userId, String question, String answer, String intent) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;

            ChatbotHistory history = new ChatbotHistory();
            history.setUser(user);
            history.setUserQuestion(question);
            history.setBotResponse(answer);
            history.setIntentDetected(intent);
            chatbotRepository.save(history);
        } catch (Exception e) {
            // Không để lỗi lưu history làm fail toàn bộ request
        }
    }
}
