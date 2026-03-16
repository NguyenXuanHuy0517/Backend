package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.logiclayer.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ChatbotController: API chatbot hỗ trợ người dùng (Mục 2.10).
 *
 * Endpoint:
 *   POST /api/business/chatbot/ask   → Gửi câu hỏi, nhận câu trả lời
 */
@RestController
@RequestMapping("/api/business/chatbot")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private ChatbotService chatbotService;

    /**
     * Xử lý câu hỏi từ người dùng.
     *
     * Body: { "question": "Hóa đơn tháng này bao nhiêu?", "userId": 5 }
     * Response: { "success": true, "data": "Hóa đơn tháng 1/2025 của bạn: ..." }
     */
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<String>> ask(@RequestBody Map<String, Object> body) {
        String question = (String) body.get("question");
        Long userId = Long.valueOf(body.get("userId").toString());
        logger.info("[CHATBOT] POST /api/business/chatbot/ask - userId: {}, question: {}", userId, question);
        
        try {
            String answer = chatbotService.processQuestion(question, userId);
            logger.info("[CHATBOT] POST /api/business/chatbot/ask - Answer generated for userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(answer));
        } catch (Exception e) {
            logger.error("[CHATBOT] POST /api/business/chatbot/ask - Error processing question for userId {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
