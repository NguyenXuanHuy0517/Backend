package com.project.datalayer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * Issue entity (cập nhật): Thêm rating và tenantFeedback (Mục 2.9).
 *
 * THAY ĐỔI SO VỚI FILE GỐC:
 * - Thêm field rating (1-5 sao) — cần chạy migration.sql để thêm cột vào DB
 * - Thêm field tenantFeedback — nhận xét của người thuê khi đánh giá
 */
@Entity
@Table(name = "issues")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private User tenant;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "image_evidence")
    private String imageEvidence;

    @ColumnDefault("'MEDIUM'")
    @Lob
    @Column(name = "priority")
    private String priority;

    @ColumnDefault("'OPEN'")
    @Lob
    @Column(name = "status")
    private String status;

    /**
     * Đánh giá mức độ hài lòng của người thuê sau khi xử lý xong (1–5 sao).
     * Null nếu chưa đánh giá.
     * Cần thêm cột: ALTER TABLE issues ADD COLUMN rating TINYINT DEFAULT NULL;
     */
    @Column(name = "rating")
    private Integer rating;

    /**
     * Nhận xét của người thuê khi xác nhận / đánh giá kết quả xử lý.
     * Cần thêm cột: ALTER TABLE issues ADD COLUMN tenant_feedback TEXT DEFAULT NULL;
     */
    @Lob
    @Column(name = "tenant_feedback")
    private String tenantFeedback;

    @ColumnDefault("current_timestamp()")
    @Column(name = "created_at")
    private Instant createdAt;

    // ── Getters & Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public User getTenant() { return tenant; }
    public void setTenant(User tenant) { this.tenant = tenant; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageEvidence() { return imageEvidence; }
    public void setImageEvidence(String imageEvidence) { this.imageEvidence = imageEvidence; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getTenantFeedback() { return tenantFeedback; }
    public void setTenantFeedback(String tenantFeedback) { this.tenantFeedback = tenantFeedback; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}