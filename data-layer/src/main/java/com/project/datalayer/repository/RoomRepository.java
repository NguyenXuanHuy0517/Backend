package com.project.datalayer.repository;

import com.project.datalayer.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * RoomRepository: Truy vấn dữ liệu phòng trọ.
 *
 * LỖI ĐÃ SỬA: File gốc có method findByRoomId(Long areaId) — tên method
 * sai ngữ nghĩa. Spring Data JPA dịch "findByRoomId" thành WHERE room_id = ?
 * tức là tìm phòng theo chính ID của nó, không phải theo khu trọ.
 *
 * Đổi thành findByAreaId để khớp với cột area_id trong bảng rooms,
 * và cập nhật MotelAreaService để dùng method mới này.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {
    /**
     * Lấy danh sách phòng theo trạng thái (AVAILABLE, RENTED, MAINTENANCE...).
     * Dùng trong dashboard thống kê tỷ lệ lấp đầy.
     */
    List<Room> findByStatus(String status);

    /**
     * Đếm số phòng trống theo khu trọ — dùng trong MotelAreaService
     * để tính thống kê nhanh mà không cần load toàn bộ danh sách.
     */
    long countByAreaIdAndStatus(Long areaId, String status);

    // Tìm danh sách phòng theo khu trọ (area_id)
    List<Room> findByAreaId(Long areaId);

    // Tìm phòng theo mã phòng (dùng cho search)
    Optional<Room> findByRoomCode(String roomCode);
}