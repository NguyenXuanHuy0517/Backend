package com.project.datalayer.mapper;

import com.project.datalayer.entity.Room;
import com.project.datalayer.dto.RoomDetailDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RoomMapper: Chuyển đổi dữ liệu phòng trọ giữa Entity và DTO.
 */
@Component
public class RoomMapper {

    public RoomDetailDTO toDetailDTO(Room entity) {
        if (entity == null) return null;

        return RoomDetailDTO.builder()
                .roomId(entity.getId())
                .roomCode(entity.getRoomCode())
                .basePrice(entity.getBasePrice())
                .elecPrice(entity.getElecPrice())
                .waterPrice(entity.getWaterPrice())
                .status(entity.getStatus())
                .areaSize(entity.getAreaSize())
                .areaName(entity.getArea() != null ? entity.getArea().getAreaName() : null)
                .address(entity.getArea() != null ? entity.getArea().getAddress() : null)
                .latitude(entity.getArea() != null && entity.getArea().getLatitude() != null
                        ? entity.getArea().getLatitude().doubleValue() : null)
                .longitude(entity.getArea() != null && entity.getArea().getLongitude() != null
                        ? entity.getArea().getLongitude().doubleValue() : null)
                // Parse chuỗi "img1.jpg,img2.jpg" → List<String>
                .images(parseCommaSeparated(entity.getImages()))
                // Parse chuỗi "Điều hoà,Nóng lạnh" → List<String>
                .amenities(parseCommaSeparated(entity.getAmenities()))
                .build();
    }

    /**
     * Chuyển chuỗi phân cách bằng dấu phẩy thành List<String>.
     * Trả về list rỗng nếu chuỗi null hoặc rỗng.
     */
    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}