package com.project.datalayer.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Invoice entity — Fix lỗi "cannot find symbol method getTotalAmount()" :92
 *
 * LỖI ĐÃ SỬA:
 * ReportService dòng :92 gọi invoice.getTotalAmount() nhưng entity không có getter.
 * Nguyên nhân: Entity dùng thủ công (không có @Data Lombok), cần khai báo getter rõ ràng.
 *
 * Cần copy file này vào:
 *   data-layer/src/main/java/com/project/datalayer/entity/Invoice.java
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column(name = "invoice_code", unique = true, length = 50)
    private String invoiceCode;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(name = "elec_old", nullable = false)
    private Integer elecOld;

    @Column(name = "elec_new", nullable = false)
    private Integer elecNew;

    @Column(name = "water_old", nullable = false)
    private Integer waterOld;

    @Column(name = "water_new", nullable = false)
    private Integer waterNew;

    @ColumnDefault("0.00")
    @Column(name = "service_fees", precision = 15, scale = 2)
    private BigDecimal serviceFees;

    /**
     * Tổng tiền hóa đơn = tiền phòng + điện + nước + dịch vụ.
     * Được tính và lưu vào DB bởi BillingService.calculateMonthlyBill().
     * Null khi mới tạo (chưa nhập chỉ số điện nước).
     */
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @ColumnDefault("'UNPAID'")
    @Column(name = "status", length = 20)
    private String status;

    @ColumnDefault("current_timestamp()")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }

    public Integer getBillingMonth() { return billingMonth; }
    public void setBillingMonth(Integer billingMonth) { this.billingMonth = billingMonth; }

    public Integer getBillingYear() { return billingYear; }
    public void setBillingYear(Integer billingYear) { this.billingYear = billingYear; }

    public Integer getElecOld() { return elecOld; }
    public void setElecOld(Integer elecOld) { this.elecOld = elecOld; }

    public Integer getElecNew() { return elecNew; }
    public void setElecNew(Integer elecNew) { this.elecNew = elecNew; }

    public Integer getWaterOld() { return waterOld; }
    public void setWaterOld(Integer waterOld) { this.waterOld = waterOld; }

    public Integer getWaterNew() { return waterNew; }
    public void setWaterNew(Integer waterNew) { this.waterNew = waterNew; }

    public BigDecimal getServiceFees() { return serviceFees; }
    public void setServiceFees(BigDecimal serviceFees) { this.serviceFees = serviceFees; }

    /** FIX: getter này bị thiếu gây lỗi ReportService :92 */
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}