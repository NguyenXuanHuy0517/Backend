package com.project.datalayer.mapper;

import com.project.datalayer.entity.Issue;
import com.project.datalayer.dto.IssueReportDTO;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * IssueMapper: Chuyển đổi giữa Issue entity và IssueReportDTO.
 *
 * Lưu ý về imageEvidence:
 * - Trong DB (Issue entity): lưu dạng String, các URL cách nhau bởi dấu phẩy
 *   Ví dụ: "https://cdn.../img1.jpg,https://cdn.../img2.jpg"
 * - Trong DTO (IssueReportDTO): lưu dạng List<String> để Flutter dễ xử lý
 *
 * Mapper chịu trách nhiệm chuyển đổi qua lại giữa 2 định dạng này.
 */
@Component
public class IssueMapper {

    public IssueReportDTO toDTO(Issue entity) {
        if (entity == null) return null;

        // Tách chuỗi URL thành List<String>
        List<String> images = Collections.emptyList();
        if (entity.getImageEvidence() != null && !entity.getImageEvidence().isBlank()) {
            images = Arrays.asList(entity.getImageEvidence().split(","));
        }

        return IssueReportDTO.builder()
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageEvidence(images)
                .priority(entity.getPriority())
                .build();
    }

    /**
     * Chuyển DTO thành entity khi người thuê gửi khiếu nại mới.
     * room và tenant sẽ được set riêng trong IssueService sau khi fetch từ DB.
     */
    public Issue toEntity(IssueReportDTO dto) {
        if (dto == null) return null;

        Issue issue = new Issue();
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setPriority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM");
        issue.setStatus("OPEN");

        // Gộp List<String> thành chuỗi phân cách bởi dấu phẩy để lưu DB
        if (dto.getImageEvidence() != null && !dto.getImageEvidence().isEmpty()) {
            issue.setImageEvidence(String.join(",", dto.getImageEvidence()));
        }

        return issue;
    }
}