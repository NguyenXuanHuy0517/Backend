package com.project.datalayer.repository;

import com.project.datalayer.entity.RoomStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * RoomStatusHistoryRepository: Truy vấn lịch sử trạng thái phòng.
 */
public interface RoomStatusHistoryRepository extends JpaRepository<RoomStatusHistory, Long> {

    /**
     * Lấy toàn bộ lịch sử của một phòng, mới nhất trước.
     * Dùng trong RoomBusinessService.getRoomStatusHistory().
     */
    List<RoomStatusHistory> findByRoomIdOrderByChangedAtDesc(Long roomId);
}