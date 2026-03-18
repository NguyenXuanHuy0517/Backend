package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.IssueReportDTO;
import com.project.datalayer.dto.IssueResponseDTO;
import com.project.logiclayer.service.IssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(IssueController.class);

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
        logger.info("[ISSUE] POST /api/business/issues - Creating issue for tenantId: {}, roomId: {}, title: {}, priority: {}",
                tenantId, dto.getRoomId(), dto.getTitle(), dto.getPriority());
        try {
            IssueResponseDTO created = issueService.createIssue(dto, tenantId);
            logger.info("[ISSUE] POST /api/business/issues - Issue created successfully with ID: {}", created.getIssueId());
            return ResponseEntity.ok(
                    ApiResponse.success("Gửi khiếu nại thành công", created));
        } catch (Exception e) {
            logger.error("[ISSUE] POST /api/business/issues - Error creating issue: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Người thuê xem danh sách khiếu nại của mình, mới nhất trước.
     */
    @GetMapping("/my/{tenantId}")
    public ResponseEntity<ApiResponse<List<IssueResponseDTO>>> getMyIssues(
            @PathVariable Long tenantId) {
        logger.info("[ISSUE] GET /api/business/issues/my/{} - Fetching issues for tenant", tenantId);
        try {
            List<IssueResponseDTO> issues = issueService.getIssuesByTenant(tenantId);
            logger.info("[ISSUE] GET /api/business/issues/my/{} - Retrieved {} issues", tenantId, issues.size());
            return ResponseEntity.ok(ApiResponse.success(issues));
        } catch (Exception e) {
            logger.error("[ISSUE] GET /api/business/issues/my/{} - Error: {}", tenantId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Chủ trọ xem tất cả khiếu nại.
     */
    @GetMapping
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<List<IssueResponseDTO>>> getAllIssues() {
        logger.info("[ISSUE] GET /api/business/issues - Fetching all issues");
        try {
            List<IssueResponseDTO> issues = issueService.getAllIssues();
            logger.info("[ISSUE] GET /api/business/issues - Retrieved {} issues", issues.size());
            return ResponseEntity.ok(ApiResponse.success(issues));
        } catch (Exception e) {
            logger.error("[ISSUE] GET /api/business/issues - Error: {}", e.getMessage(), e);
            throw e;
        }
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
        logger.info("[ISSUE] PATCH /api/business/issues/{}/status - Updating status to: {}", id, status);
        try {
            IssueResponseDTO updated = issueService.updateIssueStatus(id, status);
            logger.info("[ISSUE] PATCH /api/business/issues/{}/status - Status updated successfully", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật trạng thái thành công", updated));
        } catch (Exception e) {
            logger.error("[ISSUE] PATCH /api/business/issues/{}/status - Error: {}", id, e.getMessage(), e);
            throw e;
        }
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
        logger.info("[ISSUE] PATCH /api/business/issues/{}/confirm - Confirming resolution for tenantId: {}", id, tenantId);
        try {
            IssueResponseDTO result = issueService.confirmResolution(id, tenantId);
            logger.info("[ISSUE] PATCH /api/business/issues/{}/confirm - Resolution confirmed successfully", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Đã xác nhận hoàn thành xử lý khiếu nại", result));
        } catch (Exception e) {
            logger.error("[ISSUE] PATCH /api/business/issues/{}/confirm - Error: {}", id, e.getMessage(), e);
            throw e;
        }
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
        logger.info("[ISSUE] PATCH /api/business/issues/{}/rate - Rating issue for tenantId: {}, rating: {}", id, tenantId, rating);
        try {
            IssueResponseDTO result = issueService.rateIssue(id, tenantId, rating, feedback);
            logger.info("[ISSUE] PATCH /api/business/issues/{}/rate - Issue rated successfully", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Cảm ơn bạn đã đánh giá!", result));
        } catch (Exception e) {
            logger.error("[ISSUE] PATCH /api/business/issues/{}/rate - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}