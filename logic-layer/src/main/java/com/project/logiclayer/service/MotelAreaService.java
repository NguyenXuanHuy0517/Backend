package com.project.logiclayer.service;

import com.project.datalayer.dto.MotelAreaDTO;
import com.project.datalayer.entity.MotelArea;
import com.project.datalayer.entity.Room;
import com.project.datalayer.repository.AreaRepository;
import com.project.datalayer.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Xử lý nghiệp vụ Khu trọ và Bản đồ (Mục 2.1)
 */
@Service
public class MotelAreaService {

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<MotelAreaDTO> getAllAreasWithStats() {
        return areaRepository.findAll().stream().map(area -> {
            List<Room> rooms = roomRepository.findByAreaId(area.getId());

            long totalRooms = rooms.size();
            long availableRooms = rooms.stream()
                    .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                    .count();

            double avgPrice = rooms.stream()
                    .mapToDouble(r -> r.getBasePrice().doubleValue())
                    .average().orElse(0.0);

            return MotelAreaDTO.builder()
                    .areaId(area.getId())
                    .areaName(area.getAreaName())
                    .address(area.getAddress())
                    .latitude(area.getLatitude().doubleValue())
                    .longitude(area.getLongitude().doubleValue())
                    .totalRooms((int) totalRooms)
                    .availableRooms((int) availableRooms)
                    .averagePrice(avgPrice)
                    .build();
        }).collect(Collectors.toList());
    }
}