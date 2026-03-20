package com.project.logiclayer.service;

import com.project.datalayer.dto.RoomDetailDTO;
import com.project.datalayer.entity.Room;
import com.project.datalayer.mapper.RoomMapper;
import com.project.datalayer.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RoomBusinessService: Xử lý các nghiệp vụ quản lý phòng (Mục 2.2).
 */
@Service
public class RoomBusinessService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMapper roomMapper;

    /**
     * Lấy danh sách tất cả phòng (dùng cho overview grid).
     */
    public List<RoomDetailDTO> getRoomsOverview() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một phòng theo ID.
     */
    public RoomDetailDTO getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));
        return roomMapper.toDetailDTO(room);
    }

    /**
     * Tìm phòng theo mã phòng (roomCode).
     */
    public RoomDetailDTO getRoomByCode(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với mã: " + roomCode));
        return roomMapper.toDetailDTO(room);
    }

    /**
     * Cập nhật thông tin phòng.
     * Chỉ cập nhật các field không null/rỗng để tránh mất dữ liệu.
     */
    @Transactional
    public RoomDetailDTO updateRoomInfo(Long roomId, RoomDetailDTO updateData) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng để cập nhật: " + roomId));

        // Chỉ cập nhật nếu giá trị mới không null
        if (updateData.getRoomCode() != null && !updateData.getRoomCode().isBlank()) {
            room.setRoomCode(updateData.getRoomCode());
        }
        if (updateData.getBasePrice() != null) {
            room.setBasePrice(updateData.getBasePrice());
        }
        if (updateData.getElecPrice() != null) {
            room.setElecPrice(updateData.getElecPrice());
        }
        if (updateData.getWaterPrice() != null) {
            room.setWaterPrice(updateData.getWaterPrice());
        }
        if (updateData.getAreaSize() != null) {
            room.setAreaSize(updateData.getAreaSize());
        }
        if (updateData.getStatus() != null && !updateData.getStatus().isBlank()) {
            room.setStatus(updateData.getStatus().toUpperCase());
        }

        // Cập nhật danh sách ảnh (List<String> → String phân cách bằng dấu phẩy)
        if (updateData.getImages() != null) {
            room.setImages(String.join(",", updateData.getImages()));
        }

        // Cập nhật tiện nghi
        if (updateData.getAmenities() != null) {
            room.setAmenities(String.join(",", updateData.getAmenities()));
        }

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toDetailDTO(savedRoom);
    }

    /**
     * Cập nhật trạng thái phòng.
     * Validate giá trị hợp lệ trước khi lưu.
     */
    @Transactional
    public void changeRoomStatus(Long roomId, String newStatus) {
        // Validate status hợp lệ
        List<String> validStatuses = Arrays.asList("AVAILABLE", "OCCUPIED", "MAINTENANCE");
        String statusUpper = newStatus.toUpperCase();
        if (!validStatuses.contains(statusUpper)) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + newStatus
                    + ". Các giá trị hợp lệ: " + validStatuses);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại: " + roomId));

        room.setStatus(statusUpper);
        roomRepository.save(room);
    }
}