package com.project.logiclayer.controller;

import com.project.logiclayer.service.RoomBusinessService;
import com.project.datalayer.dto.RoomDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RoomController: Cung cấp API quản lý phòng cho ứng dụng Flutter.
 * Kết nối trực tiếp với RoomBusinessService để xử lý logic nghiệp vụ.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/business/rooms")
public class RoomController {

    @Autowired
    private RoomBusinessService roomBusinessService;

    /**
     * API: Lấy danh sách tổng quan tất cả các phòng (Mục 2.2).
     * Trả về danh sách DTO chứa thông tin cơ bản để hiển thị dạng Grid/List trên App.
     */
    @GetMapping("/overview")
    public ResponseEntity<List<RoomDetailDTO>> getRoomsOverview() {
        List<RoomDetailDTO> rooms = roomBusinessService.getRoomsOverview();
        return ResponseEntity.ok(rooms);
    }

    /**
     * API: Xem thông tin chi tiết một phòng cụ thể.
     * Bao gồm: Trang thiết bị, diện tích, giá điện nước và thông tin khu trọ.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomDetailDTO> getRoomDetail(@PathVariable Long id) {
        RoomDetailDTO roomDetail = roomBusinessService.getRoomDetail(id);
        return ResponseEntity.ok(roomDetail);
    }

    /**
     * API: Tìm phòng theo mã phòng.
     */
    @GetMapping("/code/{roomCode}")
    public ResponseEntity<RoomDetailDTO> getRoomByCode(@PathVariable String roomCode) {
        RoomDetailDTO roomDetail = roomBusinessService.getRoomByCode(roomCode);
        return ResponseEntity.ok(roomDetail);
    }

    /**
     * API: Chỉnh sửa thông tin phòng (Mục 2.2).
     * Nhận JSON từ App Flutter và cập nhật vào Database.
     * App sẽ gửi danh sách images và amenities dạng List<String>.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoomDetailDTO> updateRoom(
            @PathVariable Long id,
            @RequestBody RoomDetailDTO roomDetailDTO) {
        RoomDetailDTO updatedRoom = roomBusinessService.updateRoomInfo(id, roomDetailDTO);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * API: Cập nhật nhanh trạng thái phòng (Ví dụ: Chuyển sang 'Đang sửa chữa').
     * Sử dụng PatchMapping để chỉ thay đổi một phần dữ liệu.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        roomBusinessService.changeRoomStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái phòng thành công.");
    }
}