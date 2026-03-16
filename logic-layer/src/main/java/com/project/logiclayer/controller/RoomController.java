package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.RoomDetailDTO;
import com.project.datalayer.dto.RoomPriceUpdateDTO;
import com.project.datalayer.dto.RoomStatusHistoryDTO;
import com.project.logiclayer.service.RoomBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RoomController (cập nhật hoàn chỉnh — Mục 2.2 + 2.3).
 *
 * Endpoint đầy đủ:
 *   GET    /api/business/rooms/overview          → Danh sách tổng quan phòng
 *   GET    /api/business/rooms/{id}              → Chi tiết phòng
 *   PUT    /api/business/rooms/{id}              → Cập nhật thông tin phòng
 *   PATCH  /api/business/rooms/{id}/status       → Đổi trạng thái phòng
 *   GET    /api/business/rooms/{id}/history      → Lịch sử trạng thái phòng  [MỚI]
 *   PUT    /api/business/rooms/{id}/prices       → Thiết lập giá điện nước   [MỚI]
 */
@RestController
@RequestMapping("/api/business/rooms")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomBusinessService roomBusinessService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<List<RoomDetailDTO>>> getRoomsOverview() {
        logger.info("[ROOM] GET /api/business/rooms/overview - Fetching rooms overview");
        try {
            List<RoomDetailDTO> rooms = roomBusinessService.getRoomsOverview();
            logger.info("[ROOM] GET /api/business/rooms/overview - Retrieved {} rooms", rooms.size());
            return ResponseEntity.ok(ApiResponse.success(rooms));
        } catch (Exception e) {
            logger.error("[ROOM] GET /api/business/rooms/overview - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDetailDTO>> getRoomDetail(@PathVariable Long id) {
        logger.info("[ROOM] GET /api/business/rooms/{} - Fetching room detail", id);
        try {
            RoomDetailDTO room = roomBusinessService.getRoomDetail(id);
            logger.info("[ROOM] GET /api/business/rooms/{} - Room detail retrieved", id);
            return ResponseEntity.ok(ApiResponse.success(room));
        } catch (Exception e) {
            logger.error("[ROOM] GET /api/business/rooms/{} - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<RoomDetailDTO>> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomDetailDTO roomDetailDTO) {
        logger.info("[ROOM] PUT /api/business/rooms/{} - Updating room", id);
        try {
            RoomDetailDTO updated = roomBusinessService.updateRoomInfo(id, roomDetailDTO);
            logger.info("[ROOM] PUT /api/business/rooms/{} - Room updated successfully", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật phòng thành công", updated));
        } catch (Exception e) {
            logger.error("[ROOM] PUT /api/business/rooms/{} - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Đổi trạng thái phòng với ghi chú lý do.
     *
     * @param id         ID phòng
     * @param status     Trạng thái mới: AVAILABLE | DEPOSITED | RENTED | MAINTENANCE
     * @param changedBy  ID người thực hiện (tùy chọn, lấy từ JWT trong production)
     * @param note       Ghi chú lý do (tùy chọn)
     */
    @PatchMapping("/{id}/status")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long changedBy,
            @RequestParam(required = false) String note) {
        logger.info("[ROOM] PATCH /api/business/rooms/{}/status - Changing status to: {}, changedBy: {}, note: {}", id, status, changedBy, note);
        try {
            roomBusinessService.changeRoomStatus(id, status, changedBy, note);
            logger.info("[ROOM] PATCH /api/business/rooms/{}/status - Status updated successfully", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật trạng thái phòng thành công", null));
        } catch (Exception e) {
            logger.error("[ROOM] PATCH /api/business/rooms/{}/status - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Xem lịch sử thay đổi trạng thái của một phòng (Mục 2.2).
     * Hiển thị mốc thời gian, trạng thái cũ → mới, người thực hiện.
     */
    @GetMapping("/{id}/history")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<List<RoomStatusHistoryDTO>>> getRoomHistory(
            @PathVariable Long id) {
        logger.info("[ROOM] GET /api/business/rooms/{}/history - Fetching room status history", id);
        try {
            List<RoomStatusHistoryDTO> history = roomBusinessService.getRoomStatusHistory(id);
            logger.info("[ROOM] GET /api/business/rooms/{}/history - Retrieved {} history records", id, history.size());
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            logger.error("[ROOM] GET /api/business/rooms/{}/history - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Thiết lập giá điện nước riêng cho phòng (Mục 2.3).
     *
     * Body: { "elecPrice": 3800, "waterPrice": 16000, "reason": "Tăng giá EVN" }
     * Chỉ truyền field cần thay đổi (null = giữ nguyên).
     */
    @PutMapping("/{id}/prices")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<RoomDetailDTO>> updateRoomPrices(
            @PathVariable Long id,
            @RequestBody RoomPriceUpdateDTO dto) {
        logger.info("[ROOM] PUT /api/business/rooms/{}/prices - Updating prices. elecPrice: {}, waterPrice: {}, reason: {}",
                id, dto.getElecPrice(), dto.getWaterPrice(), dto.getReason());
        try {
            RoomDetailDTO updated = roomBusinessService.updateRoomPrices(id, dto);
            logger.info("[ROOM] PUT /api/business/rooms/{}/prices - Prices updated successfully", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật giá điện nước thành công", updated));
        } catch (Exception e) {
            logger.error("[ROOM] PUT /api/business/rooms/{}/prices - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}