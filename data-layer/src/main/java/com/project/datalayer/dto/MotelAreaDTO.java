package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MotelAreaDTO: Dùng để trả về thông tin tổng quan khu trọ cho Dashboard (Mục 2.1)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MotelAreaDTO {
    private Long areaId;
    private String areaName;
    private String address;
    private Double latitude;
    private Double longitude;

    // Các trường thống kê bổ sung cho giao diện
    private Integer totalRooms;      // Tổng số phòng
    private Integer availableRooms;  // Số phòng còn trống
    private Double averagePrice;    // Giá thuê trung bình của khu
}