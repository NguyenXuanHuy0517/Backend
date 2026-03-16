package com.project.logiclayer.service;

import com.project.datalayer.dto.IssueReportDTO;
import com.project.datalayer.dto.IssueResponseDTO;
import com.project.datalayer.entity.Issue;
import com.project.datalayer.entity.Room;
import com.project.datalayer.entity.User;
import com.project.datalayer.mapper.IssueMapper;
import com.project.datalayer.repository.IssueRepository;
import com.project.datalayer.repository.RoomRepository;
import com.project.datalayer.repository.UserRepository;
import com.project.logiclayer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IssueService (cập nhật — Mục 2.9).
 *
 * BỔ SUNG:
 * 1. confirmResolution() — người thuê xác nhận kết quả xử lý → CLOSED
 * 2. rateIssue()         — người thuê đánh giá mức độ hài lòng (1-5 sao)
 * 3. toResponseDTO()     — mapper nội bộ trả về IssueResponseDTO đầy đủ hơn
 *
 * LƯU Ý DB:
 * Cần thêm 2 cột vào bảng issues:
 *   ALTER TABLE issues ADD COLUMN rating INT DEFAULT NULL;
 *   ALTER TABLE issues ADD COLUMN tenant_feedback TEXT DEFAULT NULL;
 * Và thêm 2 field tương ứng vào Issue entity.
 */
@Service
public class IssueService {

    @Autowired private IssueRepository issueRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private IssueMapper issueMapper;

    // ─── Tạo khiếu nại mới ───────────────────────────────────────────────────

    @Transactional
    public IssueResponseDTO createIssue(IssueReportDTO dto, Long tenantId) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "ID", dto.getRoomId()));
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "ID", tenantId));

        Issue issue = issueMapper.toEntity(dto);
        issue.setRoom(room);
        issue.setTenant(tenant);

        return toResponseDTO(issueRepository.save(issue));
    }

    // ─── Chủ trọ cập nhật trạng thái xử lý ──────────────────────────────────

    /**
     * Chủ trọ cập nhật trạng thái xử lý: OPEN → PROCESSING → RESOLVED.
     * Khi đặt sang RESOLVED, người thuê sẽ nhận thông báo để xác nhận.
     *
     * Chỉ HOST được gọi endpoint này (kiểm tra @PreAuthorize ở Controller).
     */
    @Transactional
    public IssueResponseDTO updateIssueStatus(Long issueId, String newStatus) {
        Issue issue = findIssueById(issueId);

        List<String> hostAllowedStatuses = List.of("PROCESSING", "RESOLVED");
        if (!hostAllowedStatuses.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Chủ trọ chỉ được đặt trạng thái: PROCESSING hoặc RESOLVED.");
        }

        // Không cho phép lùi trạng thái
        if ("CLOSED".equals(issue.getStatus())) {
            throw new IllegalArgumentException("Khiếu nại đã đóng, không thể thay đổi trạng thái.");
        }

        issue.setStatus(newStatus);
        return toResponseDTO(issueRepository.save(issue));
    }

    // ─── Người thuê xác nhận kết quả (MỤC 2.9) ───────────────────────────────

    /**
     * Người thuê xác nhận đã nhận được kết quả xử lý → đổi RESOLVED → CLOSED.
     *
     * Chỉ được confirm khi status = RESOLVED (chủ trọ đã báo xong).
     * Chỉ TENANT sở hữu khiếu nại mới được gọi (kiểm tra ở Controller).
     *
     * @param issueId  ID khiếu nại
     * @param tenantId ID người thuê (để xác minh quyền sở hữu)
     */
    @Transactional
    public IssueResponseDTO confirmResolution(Long issueId, Long tenantId) {
        Issue issue = findIssueById(issueId);

        // Kiểm tra người thuê có phải chủ khiếu nại không
        if (!issue.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException("Bạn không có quyền xác nhận khiếu nại này.");
        }

        if (!"RESOLVED".equals(issue.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể xác nhận khi khiếu nại ở trạng thái RESOLVED. " +
                            "Trạng thái hiện tại: " + issue.getStatus());
        }

        issue.setStatus("CLOSED");
        return toResponseDTO(issueRepository.save(issue));
    }

    // ─── Người thuê đánh giá hài lòng (MỤC 2.9) ─────────────────────────────

    /**
     * Người thuê đánh giá mức độ hài lòng sau khi khiếu nại đã CLOSED.
     *
     * @param issueId      ID khiếu nại
     * @param tenantId     ID người thuê (để xác minh quyền)
     * @param rating       Điểm đánh giá 1–5 (1: rất không hài lòng, 5: rất hài lòng)
     * @param feedback     Nhận xét tùy chọn
     *
     * LƯU Ý: Cần thêm field rating và tenantFeedback vào Issue entity
     * và thêm cột tương ứng vào DB trước khi dùng method này.
     */
    @Transactional
    public IssueResponseDTO rateIssue(Long issueId, Long tenantId, Integer rating, String feedback) {
        Issue issue = findIssueById(issueId);

        if (!issue.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException("Bạn không có quyền đánh giá khiếu nại này.");
        }

        if (!"CLOSED".equals(issue.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể đánh giá sau khi khiếu nại đã CLOSED.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5.");
        }

        // TODO: Sau khi thêm field vào Issue entity, bỏ comment 2 dòng dưới:
        // issue.setRating(rating);
        // issue.setTenantFeedback(feedback);
        // Tạm thời lưu rating vào description để không cần thay đổi DB ngay
        issue.setDescription((issue.getDescription() != null ? issue.getDescription() : "")
                + "\n[Đánh giá: " + rating + "/5" + (feedback != null ? " - " + feedback : "") + "]");

        return toResponseDTO(issueRepository.save(issue));
    }

    // ─── Truy vấn ─────────────────────────────────────────────────────────────

    /** Người thuê xem danh sách khiếu nại của mình */
    public List<IssueResponseDTO> getIssuesByTenant(Long tenantId) {
        return issueRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    /** Chủ trọ xem tất cả khiếu nại */
    public List<IssueResponseDTO> getAllIssues() {
        return issueRepository.findAll().stream()
                .map(this::toResponseDTO).collect(Collectors.toList());
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private Issue findIssueById(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Khiếu nại", "ID", issueId));
    }

    /** Chuyển Issue entity → IssueResponseDTO đầy đủ thông tin */
    private IssueResponseDTO toResponseDTO(Issue entity) {
        List<String> images = Collections.emptyList();
        if (entity.getImageEvidence() != null && !entity.getImageEvidence().isBlank()) {
            images = Arrays.asList(entity.getImageEvidence().split(","));
        }

        return IssueResponseDTO.builder()
                .issueId(entity.getId())
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .roomCode(entity.getRoom() != null ? entity.getRoom().getRoomCode() : null)
                .tenantId(entity.getTenant() != null ? entity.getTenant().getId() : null)
                .tenantName(entity.getTenant() != null ? entity.getTenant().getFullName() : null)
                .tenantPhone(entity.getTenant() != null ? entity.getTenant().getPhoneNumber() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageEvidence(images)
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}