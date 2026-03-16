package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * RoomStatusHistoryDTO: Một mốc thay đổi trạng thái trong lịch sử phòng.
 *
 * Dùng cho:
 *   GET /api/business/rooms/{id}/history → Chủ trọ xem lịch sử trạng thái phòng
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomStatusHistoryDTO {

    private Long historyId;
    private Long roomId;
    private String roomCode;

    /** Trạng thái trước khi thay đổi (null nếu là lần đầu) */
    private String oldStatus;

    /** Trạng thái sau khi thay đổi */
    private String newStatus;

    /** Người thực hiện thay đổi (null = hệ thống tự động) */
    private String changedByName;

    /** Ghi chú lý do (vd: "Ký hợp đồng HD-2024-001") */
    private String note;

    private Instant changedAt;
}