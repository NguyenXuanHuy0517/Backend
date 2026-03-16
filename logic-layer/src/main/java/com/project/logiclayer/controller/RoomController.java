package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.RoomDetailDTO;
import com.project.datalayer.dto.RoomPriceUpdateDTO;
import com.project.datalayer.dto.RoomStatusHistoryDTO;
import com.project.logiclayer.service.RoomBusinessService;
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

    @Autowired
    private RoomBusinessService roomBusinessService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<List<RoomDetailDTO>>> getRoomsOverview() {
        return ResponseEntity.ok(
                ApiResponse.success(roomBusinessService.getRoomsOverview()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDetailDTO>> getRoomDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(roomBusinessService.getRoomDetail(id)));
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<RoomDetailDTO>> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomDetailDTO roomDetailDTO) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật phòng thành công",
                        roomBusinessService.updateRoomInfo(id, roomDetailDTO)));
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
        roomBusinessService.changeRoomStatus(id, status, changedBy, note);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật trạng thái phòng thành công", null));
    }

    /**
     * Xem lịch sử thay đổi trạng thái của một phòng (Mục 2.2).
     * Hiển thị mốc thời gian, trạng thái cũ → mới, người thực hiện.
     */
    @GetMapping("/{id}/history")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<List<RoomStatusHistoryDTO>>> getRoomHistory(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(roomBusinessService.getRoomStatusHistory(id)));
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
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật giá điện nước thành công",
                        roomBusinessService.updateRoomPrices(id, dto)));
    }
}