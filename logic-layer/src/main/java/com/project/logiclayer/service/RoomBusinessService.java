package com.project.logiclayer.service;

import com.project.datalayer.dto.RoomDetailDTO;
import com.project.datalayer.entity.Room;
import com.project.datalayer.mapper.RoomMapper;
import com.project.datalayer.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RoomBusinessService: Xử lý các nghiệp vụ quản lý phòng (Mục 2.2).
 * Phụ trách cung cấp dữ liệu tổng quan, chi tiết và chỉnh sửa phòng.
 */
@Service
public class RoomBusinessService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMapper roomMapper;

    /**
     * Lấy thông tin tổng quan cho từng phòng trong danh sách (2.2).
     * Trả về danh sách DTO để hiển thị trên UI.
     */
    public List<RoomDetailDTO> getRoomsOverview() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết thông tin một phòng (bao gồm trang thiết bị, diện tích, trạng thái).
     */
    public RoomDetailDTO getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));
        return roomMapper.toDetailDTO(room);
    }

    /**
     * Chỉnh sửa thông tin phòng (Mã phòng, giá thuê, diện tích, thiết bị) (2.2).
     * Cập nhật các trường khớp với cấu trúc RoomDetailDTO mới.
     */
    @Transactional
    public RoomDetailDTO updateRoomInfo(Long roomId, RoomDetailDTO updateData) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng để cập nhật"));

        // Cập nhật các trường thông tin cơ bản dựa trên DTO mới
        room.setRoomCode(updateData.getRoomCode());
        room.setBasePrice(updateData.getBasePrice());
        room.setElecPrice(updateData.getElecPrice());
        room.setWaterPrice(updateData.getWaterPrice());
        room.setAreaSize(updateData.getAreaSize());
        room.setStatus(updateData.getStatus());

        /// Cập nhật danh sách ảnh (Chuyển List<String> thành String cách nhau bởi dấu phẩy)
        if (updateData.getImages() != null) {
            String imagesString = String.join(",", updateData.getImages());
            room.setImages(imagesString);
        }

        // Cập nhật tiện ích (Chuyển List<String> thành String cách nhau bởi dấu phẩy)
        if (updateData.getAmenities() != null) {
            String amenitiesString = String.join(",", updateData.getAmenities());
            room.setAmenities(amenitiesString);
        }

        // Lưu thông tin mới vào Database
        Room savedRoom = roomRepository.save(room);
        return roomMapper.toDetailDTO(savedRoom);
    }

    /**
     * Cập nhật trạng thái phòng (Trống, Đang thuê, Đang sửa chữa).
     * Thường dùng khi chủ trọ muốn bảo trì hoặc hệ thống tự động đổi khi có hợp đồng.
     */
    @Transactional
    public void changeRoomStatus(Long roomId, String newStatus) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // Cập nhật trạng thái trực tiếp
        room.setStatus(newStatus);
        roomRepository.save(room);
    }
}