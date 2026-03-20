package com.project.logiclayer.service;

import com.project.datalayer.dto.MotelAreaDTO;
import com.project.datalayer.entity.MotelArea;
import com.project.datalayer.entity.Room;
import com.project.datalayer.repository.AreaRepository;
import com.project.datalayer.repository.RoomRepository;
import com.project.logiclayer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MotelAreaService: Xử lý nghiệp vụ Khu trọ và Bản đồ (Mục 2.1).
 */
@Service
public class MotelAreaService {

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<MotelAreaDTO> getAllAreasWithStats() {
        return areaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một khu trọ theo ID.
     */
    public MotelAreaDTO getAreaDetail(Long areaId) {
        MotelArea area = areaRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("Khu trọ", "ID", areaId));
        return toDTO(area);
    }

    /**
     * Tạo khu trọ mới.
     */
    @Transactional
    public MotelAreaDTO createArea(MotelAreaDTO dto) {
        MotelArea area = new MotelArea();
        area.setAreaName(dto.getAreaName());
        area.setAddress(dto.getAddress());
        if (dto.getLatitude() != null) area.setLatitude(BigDecimal.valueOf(dto.getLatitude()));
        if (dto.getLongitude() != null) area.setLongitude(BigDecimal.valueOf(dto.getLongitude()));
        return toDTO(areaRepository.save(area));
    }

    /**
     * Cập nhật tên, địa chỉ, tọa độ của khu trọ.
     */
    @Transactional
    public MotelAreaDTO updateArea(Long areaId, MotelAreaDTO dto) {
        MotelArea area = areaRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("Khu trọ", "ID", areaId));
        if (dto.getAreaName() != null) area.setAreaName(dto.getAreaName());
        if (dto.getAddress() != null) area.setAddress(dto.getAddress());
        if (dto.getLatitude() != null) area.setLatitude(BigDecimal.valueOf(dto.getLatitude()));
        if (dto.getLongitude() != null) area.setLongitude(BigDecimal.valueOf(dto.getLongitude()));
        return toDTO(areaRepository.save(area));
    }

    // --- Private helper ---
    private MotelAreaDTO toDTO(MotelArea area) {
        var rooms = roomRepository.findByAreaId(area.getId());
        long totalRooms = rooms.size();
        long availableRooms = rooms.stream()
                .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .count();
        double avgPrice = rooms.stream()
                .mapToDouble(r -> r.getBasePrice().doubleValue())
                .average()
                .orElse(0.0);

        return MotelAreaDTO.builder()
                .areaId(area.getId())
                .areaName(area.getAreaName())
                .address(area.getAddress())
                .latitude(area.getLatitude() != null ? area.getLatitude().doubleValue() : null)
                .longitude(area.getLongitude() != null ? area.getLongitude().doubleValue() : null)
                .totalRooms((int) totalRooms)
                .availableRooms((int) availableRooms)
                .averagePrice(avgPrice)
                .build();
    }
}