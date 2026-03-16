package com.project.logiclayer.service;

import com.project.datalayer.dto.RoomDetailDTO;
import com.project.datalayer.dto.RoomPriceUpdateDTO;
import com.project.datalayer.dto.RoomStatusHistoryDTO;
import com.project.datalayer.entity.Room;
import com.project.datalayer.entity.RoomStatusHistory;
import com.project.datalayer.entity.User;
import com.project.datalayer.mapper.RoomMapper;
import com.project.datalayer.repository.RoomRepository;
import com.project.datalayer.repository.RoomStatusHistoryRepository;
import com.project.datalayer.repository.UserRepository;
import com.project.logiclayer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RoomBusinessService (cập nhật hoàn chỉnh — Mục 2.2 + 2.3).
 *
 * BỔ SUNG SO VỚI FILE GỐC:
 * 1. getRoomStatusHistory()  — lịch sử trạng thái phòng (Mục 2.2)
 * 2. changeRoomStatus() — ghi lịch sử mỗi khi đổi trạng thái (Mục 2.2)
 * 3. updateRoomPrices() — set giá điện nước riêng cho phòng (Mục 2.3)
 *
 * Các method cũ (getRoomsOverview, getRoomDetail, updateRoomInfo)
 * giữ nguyên logic, chỉ đổi exception sang ResourceNotFoundException.
 */
@Service
public class RoomBusinessService {

    @Autowired private RoomRepository roomRepository;
    @Autowired private RoomMapper roomMapper;
    @Autowired private RoomStatusHistoryRepository historyRepository;
    @Autowired private UserRepository userRepository;

    // ─── Lấy danh sách / chi tiết phòng ─────────────────────────────────────

    public List<RoomDetailDTO> getRoomsOverview() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDetailDTO)
                .collect(Collectors.toList());
    }

    public RoomDetailDTO getRoomDetail(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "ID", roomId));
        return roomMapper.toDetailDTO(room);
    }

    // ─── Cập nhật thông tin phòng ────────────────────────────────────────────

    @Transactional
    public RoomDetailDTO updateRoomInfo(Long roomId, RoomDetailDTO updateData) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "ID", roomId));

        room.setRoomCode(updateData.getRoomCode());
        room.setBasePrice(updateData.getBasePrice());
        room.setElecPrice(updateData.getElecPrice());
        room.setWaterPrice(updateData.getWaterPrice());
        room.setAreaSize(updateData.getAreaSize());

        if (updateData.getImages() != null) {
            room.setImages(String.join(",", updateData.getImages()));
        }
        if (updateData.getAmenities() != null) {
            room.setAmenities(String.join(",", updateData.getAmenities()));
        }

        return roomMapper.toDetailDTO(roomRepository.save(room));
    }

    // ─── Đổi trạng thái phòng + ghi lịch sử (Mục 2.2) ───────────────────────

    /**
     * Cập nhật trạng thái phòng và tự động ghi một bản ghi vào lịch sử.
     *
     * @param roomId    ID phòng cần đổi trạng thái
     * @param newStatus Trạng thái mới: AVAILABLE | DEPOSITED | RENTED | MAINTENANCE
     * @param changedById ID người thực hiện (null nếu do hệ thống tự động)
     * @param note      Ghi chú lý do thay đổi (tùy chọn)
     */
    @Transactional
    public void changeRoomStatus(Long roomId, String newStatus,
                                 Long changedById, String note) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "ID", roomId));

        List<String> validStatuses = List.of("AVAILABLE", "DEPOSITED", "RENTED", "MAINTENANCE");
        if (!validStatuses.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ: " + newStatus + ". Hợp lệ: " + validStatuses);
        }

        String oldStatus = room.getStatus();

        // Ghi lịch sử TRƯỚC khi thay đổi để có oldStatus đúng
        RoomStatusHistory history = new RoomStatusHistory();
        history.setRoom(room);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);

        // Gán người thực hiện nếu có
        if (changedById != null) {
            userRepository.findById(changedById)
                    .ifPresent(history::setChangedBy);
        }

        historyRepository.save(history);

        // Sau đó mới cập nhật trạng thái phòng
        room.setStatus(newStatus);
        roomRepository.save(room);
    }

    /**
     * Overload ngắn gọn — dùng cho các chỗ gọi nội bộ (scheduler, contract service)
     * mà không cần truyền changedById và note.
     */
    @Transactional
    public void changeRoomStatus(Long roomId, String newStatus) {
        changeRoomStatus(roomId, newStatus, null, "Thay đổi tự động bởi hệ thống");
    }

    // ─── Lịch sử trạng thái phòng (Mục 2.2) ─────────────────────────────────

    /**
     * Lấy toàn bộ lịch sử thay đổi trạng thái của một phòng, mới nhất trước.
     */
    public List<RoomStatusHistoryDTO> getRoomStatusHistory(Long roomId) {
        // Kiểm tra phòng tồn tại
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Phòng", "ID", roomId);
        }

        return historyRepository.findByRoomIdOrderByChangedAtDesc(roomId)
                .stream()
                .map(h -> RoomStatusHistoryDTO.builder()
                        .historyId(h.getId())
                        .roomId(h.getRoom().getId())
                        .roomCode(h.getRoom().getRoomCode())
                        .oldStatus(h.getOldStatus())
                        .newStatus(h.getNewStatus())
                        .changedByName(h.getChangedBy() != null
                                ? h.getChangedBy().getFullName() : "Hệ thống")
                        .note(h.getNote())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Cập nhật giá điện nước riêng cho phòng (Mục 2.3) ───────────────────

    /**
     * Chủ trọ thiết lập giá điện nước riêng cho một phòng cụ thể.
     *
     * Theo Mục 2.3, thứ tự ưu tiên áp dụng giá:
     *   1. Giá theo hợp đồng (nếu có — hiện chưa implement, phase sau)
     *   2. Giá theo phòng (method này cập nhật)
     *
     * Chỉ cập nhật field nào được truyền vào (không null).
     * Không ảnh hưởng đến các hóa đơn đã tạo — chỉ áp dụng cho hóa đơn mới.
     *
     * @param roomId ID phòng
     * @param dto    Giá điện và/hoặc giá nước mới
     */
    @Transactional
    public RoomDetailDTO updateRoomPrices(Long roomId, RoomPriceUpdateDTO dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "ID", roomId));

        if (dto.getElecPrice() != null) {
            if (dto.getElecPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Giá điện phải lớn hơn 0.");
            }
            room.setElecPrice(dto.getElecPrice());
        }

        if (dto.getWaterPrice() != null) {
            if (dto.getWaterPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Giá nước phải lớn hơn 0.");
            }
            room.setWaterPrice(dto.getWaterPrice());
        }

        roomRepository.save(room);

        // Ghi lịch sử thay đổi giá vào room_status_history với note mô tả
        String priceNote = buildPriceChangeNote(dto);
        RoomStatusHistory history = new RoomStatusHistory();
        history.setRoom(room);
        history.setOldStatus(room.getStatus());
        history.setNewStatus(room.getStatus()); // trạng thái không đổi
        history.setNote(priceNote);
        historyRepository.save(history);

        return roomMapper.toDetailDTO(room);
    }

    private String buildPriceChangeNote(RoomPriceUpdateDTO dto) {
        StringBuilder sb = new StringBuilder("[Thay đổi giá] ");
        if (dto.getElecPrice() != null) {
            sb.append("Giá điện: ").append(dto.getElecPrice()).append("đ/kWh. ");
        }
        if (dto.getWaterPrice() != null) {
            sb.append("Giá nước: ").append(dto.getWaterPrice()).append("đ/m³. ");
        }
        if (dto.getReason() != null && !dto.getReason().isBlank()) {
            sb.append("Lý do: ").append(dto.getReason());
        }
        return sb.toString().trim();
    }
}