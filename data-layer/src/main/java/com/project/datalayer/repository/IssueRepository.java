package com.project.datalayer.repository;

import com.project.datalayer.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * IssueRepository — Fix đủ method.
 *
 * LỖI ĐÃ SỬA:
 * "cannot find symbol method findByTenantIdOrderByCreatedAtDesc(Long)" — IssueService :161
 * "cannot find symbol method countByStatus(String)"                    — ReportService :66-69
 * "cannot find symbol method countByPriority(String)"                  — ReportService :195
 *
 * Nguyên nhân: File IssueRepository cũ trong data-layer chưa được thay thế
 * bằng file mới từ thư mục supplement/. Cần copy file này vào:
 *   data-layer/src/main/java/com/project/datalayer/repository/IssueRepository.java
 */
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Lấy tất cả khiếu nại của người thuê, mới nhất trước.
     * Spring Data tự sinh: SELECT * FROM issues WHERE tenant_id = ? ORDER BY created_at DESC
     */
    List<Issue> findByTenant_IdOrderByCreatedAtDesc(Long tenantId);

    /**
     * Lấy khiếu nại theo trạng thái (OPEN / PROCESSING / RESOLVED / CLOSED).
     */
    List<Issue> findByStatus(String status);

    /**
     * Đếm khiếu nại theo trạng thái — ReportService dùng để thống kê.
     * Spring Data tự sinh: SELECT COUNT(*) FROM issues WHERE status = ?
     */
    long countByStatus(String status);

    /**
     * Đếm khiếu nại theo mức ưu tiên (LOW / MEDIUM / HIGH / URGENT).
     * Spring Data tự sinh: SELECT COUNT(*) FROM issues WHERE priority = ?
     */
    long countByPriority(String priority);
}