package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.IssueReportDTO;
import com.project.datalayer.dto.IssueResponseDTO;
import com.project.logiclayer.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IssueController (cập nhật hoàn chỉnh — Mục 2.9).
 *
 * Endpoint đầy đủ:
 *   POST   /api/business/issues                      → Gửi khiếu nại mới
 *   GET    /api/business/issues/my/{tenantId}        → Người thuê xem của mình
 *   GET    /api/business/issues                      → Chủ trọ xem tất cả
 *   PATCH  /api/business/issues/{id}/status          → Chủ trọ cập nhật trạng thái
 *   PATCH  /api/business/issues/{id}/confirm         → Người thuê xác nhận     [MỚI]
 *   PATCH  /api/business/issues/{id}/rate            → Người thuê đánh giá     [MỚI]
 */
@RestController
@RequestMapping("/api/business/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    /**
     * Người thuê gửi khiếu nại mới.
     * Body: { "roomId": 1, "title": "Điều hòa hỏng", "description": "...",
     *         "imageEvidence": ["url1", "url2"], "priority": "HIGH" }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IssueResponseDTO>> createIssue(
            @RequestBody IssueReportDTO dto,
            @RequestParam Long tenantId) {
        IssueResponseDTO created = issueService.createIssue(dto, tenantId);
        return ResponseEntity.ok(
                ApiResponse.success("Gửi khiếu nại thành công", created));
    }

    /**
     * Người thuê xem danh sách khiếu nại của mình, mới nhất trước.
     */
    @GetMapping("/my/{tenantId}")
    public ResponseEntity<ApiResponse<List<IssueResponseDTO>>> getMyIssues(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(
                ApiResponse.success(issueService.getIssuesByTenant(tenantId)));
    }

    /**
     * Chủ trọ xem tất cả khiếu nại.
     */
    @GetMapping
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<List<IssueResponseDTO>>> getAllIssues() {
        return ResponseEntity.ok(
                ApiResponse.success(issueService.getAllIssues()));
    }

    /**
     * Chủ trọ cập nhật tiến độ xử lý.
     * status chỉ nhận: PROCESSING hoặc RESOLVED
     */
    @PatchMapping("/{id}/status")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<IssueResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        IssueResponseDTO updated = issueService.updateIssueStatus(id, status);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật trạng thái thành công", updated));
    }

    /**
     * Người thuê xác nhận đã nhận được kết quả xử lý → đổi RESOLVED → CLOSED.
     *
     * Chỉ gọi được khi status = RESOLVED.
     * tenantId truyền qua param — trong production nên lấy từ JWT token.
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<IssueResponseDTO>> confirmResolution(
            @PathVariable Long id,
            @RequestParam Long tenantId) {
        IssueResponseDTO result = issueService.confirmResolution(id, tenantId);
        return ResponseEntity.ok(
                ApiResponse.success("Đã xác nhận hoàn thành xử lý khiếu nại", result));
    }

    /**
     * Người thuê đánh giá mức độ hài lòng sau khi khiếu nại đã CLOSED.
     *
     * Body: { "rating": 4, "feedback": "Xử lý nhanh, cảm ơn!" }
     * rating: 1 (rất không hài lòng) → 5 (rất hài lòng)
     */
    @PatchMapping("/{id}/rate")
    public ResponseEntity<ApiResponse<IssueResponseDTO>> rateIssue(
            @PathVariable Long id,
            @RequestParam Long tenantId,
            @RequestBody Map<String, Object> body) {
        Integer rating = (Integer) body.get("rating");
        String feedback = (String) body.getOrDefault("feedback", null);
        IssueResponseDTO result = issueService.rateIssue(id, tenantId, rating, feedback);
        return ResponseEntity.ok(
                ApiResponse.success("Cảm ơn bạn đã đánh giá!", result));
    }
}