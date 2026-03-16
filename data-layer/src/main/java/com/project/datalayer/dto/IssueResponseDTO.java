package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

/**
 * IssueResponseDTO: Phiên bản đầy đủ của IssueReportDTO,
 * trả về cho cả chủ trọ lẫn người thuê khi xem chi tiết khiếu nại.
 *
 * Khác với IssueReportDTO (chỉ dùng để tạo mới):
 * - Có issueId, status, createdAt để theo dõi
 * - Có tenantName để chủ trọ biết ai gửi
 * - Có rating để đánh giá hài lòng sau khi xử lý xong
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueResponseDTO {

    private Long issueId;

    // Thông tin phòng
    private Long roomId;
    private String roomCode;

    // Thông tin người thuê gửi khiếu nại
    private Long tenantId;
    private String tenantName;
    private String tenantPhone;

    // Nội dung khiếu nại
    private String title;
    private String description;
    private List<String> imageEvidence;

    /**
     * Mức độ ưu tiên: LOW | MEDIUM | HIGH | URGENT
     */
    private String priority;

    /**
     * Trạng thái xử lý:
     *   OPEN        → Vừa gửi, chưa có ai tiếp nhận
     *   PROCESSING  → Chủ trọ đang xử lý
     *   RESOLVED    → Chủ trọ báo đã xong, chờ người thuê xác nhận
     *   CLOSED      → Người thuê đã xác nhận hoàn thành
     */
    private String status;

    /**
     * Đánh giá của người thuê sau khi xử lý xong (1–5 sao).
     * Null nếu chưa đánh giá hoặc chưa CLOSED.
     * Tương ứng với cột rating trong bảng issues (cần thêm vào DB).
     */
    private Integer rating;

    /**
     * Ghi chú phản hồi từ người thuê khi xác nhận/đánh giá.
     */
    private String tenantFeedback;

    private Instant createdAt;
}