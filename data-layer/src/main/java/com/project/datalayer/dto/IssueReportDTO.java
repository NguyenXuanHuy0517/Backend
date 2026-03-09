package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * IssueReportDTO: Dùng để gửi báo cáo sự cố từ người thuê.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueReportDTO {
    private Long roomId;
    private String title;
    private String description;
    private List<String> imageEvidence; // Gửi mảng link ảnh lên
    private String priority;
}
