package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.logiclayer.service.ChatbotService;
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

        String answer = chatbotService.processQuestion(question, userId);
        return ResponseEntity.ok(ApiResponse.success(answer));
    }
}
