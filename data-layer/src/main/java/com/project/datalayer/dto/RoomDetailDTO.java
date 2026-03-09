package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * RoomDetailDTO: Hiển thị chi tiết phòng kèm thông tin khu trọ.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDetailDTO {
    private Long roomId;
    private String roomCode;
    private BigDecimal basePrice;
    private BigDecimal elecPrice;
    private BigDecimal waterPrice;
    private String status;
    private Float areaSize;
    private List<String> images; // Danh sách URL ảnh
    private List<String> amenities; // Danh sách tiện ích

    // Thông tin từ MotelArea
    private String areaName;
    private String address;
    private Double latitude;
    private Double longitude;
}
