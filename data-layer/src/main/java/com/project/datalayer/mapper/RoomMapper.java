package com.project.datalayer.mapper;

import com.project.cruddata.entity.Room;
import com.project.cruddata.dto.RoomDetailDTO;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

/**
 * RoomMapper: Chuyển đổi dữ liệu phòng trọ giữa Entity và DTO.
 */
@Component
public class RoomMapper {

    /**
     * Chuyển đổi từ Room Entity sang RoomDetailDTO để hiển thị chi tiết trên App.
     */
    public RoomDetailDTO toDetailDTO(Room entity) {
        if (entity == null) return null;

        return RoomDetailDTO.builder()
                .roomId(entity.getId())
                .roomCode(entity.getRoomCode())
                .basePrice(entity.getBasePrice())
                .elecPrice(entity.getElecPrice())
                .waterPrice(entity.getWaterPrice())
                // Chuyển Enum status sang String để hiển thị
                .status(entity.getStatus())
                .areaSize(entity.getAreaSize())
                // Lấy thông tin từ MotelArea (Quan hệ ManyToOne)
                .areaName(entity.getArea() != null ? entity.getArea().getAreaName() : null)
                .address(entity.getArea() != null ? entity.getArea().getAddress() : null)
                .latitude(entity.getArea() != null && entity.getArea().getLatitude() != null
                        ? entity.getArea().getLatitude().doubleValue() : null)
                .longitude(entity.getArea() != null && entity.getArea().getLongitude() != null
                        ? entity.getArea().getLongitude().doubleValue() : null)
                // Xử lý danh sách ảnh và tiện ích (Nếu Entity lưu dạng List)
                .images(new ArrayList<>())
                .amenities(new ArrayList<>())
                .build();
    }
}