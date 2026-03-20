package com.project.logiclayer.controller;

import com.project.logiclayer.service.RoomBusinessService;
import com.project.datalayer.dto.RoomDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RoomController: Cung cấp API quản lý phòng cho ứng dụng Flutter.
 */
@RestController
@RequestMapping("/api/business/rooms")
public class RoomController {

    @Autowired
    private RoomBusinessService roomBusinessService;

    /**
     * GET /api/business/rooms/overview
     * Lấy danh sách tổng quan tất cả phòng.
     */
    @GetMapping("/overview")
    public ResponseEntity<List<RoomDetailDTO>> getRoomsOverview() {
        return ResponseEntity.ok(roomBusinessService.getRoomsOverview());
    }

    /**
     * GET /api/business/rooms/{id}
     * Xem chi tiết một phòng theo ID.
     * Phải khai báo TRƯỚC /code/{roomCode} để tránh conflict.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomDetail(@PathVariable Long id) {
        try {
            RoomDetailDTO room = roomBusinessService.getRoomDetail(id);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/business/rooms/code/{roomCode}
     * Tìm phòng theo mã phòng (dùng cho thanh search).
     */
    @GetMapping("/code/{roomCode}")
    public ResponseEntity<?> getRoomByCode(@PathVariable String roomCode) {
        try {
            RoomDetailDTO room = roomBusinessService.getRoomByCode(roomCode);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Không tìm thấy phòng với mã: " + roomCode));
        }
    }

    /**
     * PUT /api/business/rooms/{id}
     * Chỉnh sửa thông tin phòng.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomDetailDTO roomDetailDTO) {
        try {
            RoomDetailDTO updatedRoom = roomBusinessService.updateRoomInfo(id, roomDetailDTO);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/business/rooms/{id}/status?status=AVAILABLE
     * Cập nhật nhanh trạng thái phòng.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            roomBusinessService.changeRoomStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công", "status", status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}