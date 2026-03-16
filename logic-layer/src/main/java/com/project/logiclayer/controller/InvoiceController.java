package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.InvoiceSummaryDTO;
import com.project.datalayer.dto.MeterReadingDTO;
import com.project.logiclayer.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InvoiceController (cập nhật hoàn chỉnh — Mục 2.4 + 2.7 + 2.8).
 *
 * Endpoint đầy đủ:
 *   GET   /api/business/invoices/my-bills/{tenantId}   → Người thuê xem hóa đơn
 *   POST  /api/business/invoices/calculate/{id}        → Tính lại tổng tiền
 *   PUT   /api/business/invoices/{id}/meters           → Nhập chỉ số điện nước  [MỚI]
 *   PATCH /api/business/invoices/{id}/pay              → Xác nhận thanh toán     [MỚI]
 *   GET   /api/business/invoices/debts                 → Xem danh sách nợ        [MỚI]
 */
@RestController
@RequestMapping("/api/business/invoices")
public class InvoiceController {

    @Autowired
    private BillingService billingService;

    /**
     * Người thuê xem danh sách hóa đơn của mình, mới nhất trước.
     */
    @GetMapping("/my-bills/{tenantId}")
    public ResponseEntity<ApiResponse<List<InvoiceSummaryDTO>>> getMyInvoices(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(
                ApiResponse.success(billingService.getInvoicesByTenant(tenantId)));
    }

    /**
     * Tính lại tổng tiền hóa đơn (dùng sau khi cập nhật chỉ số).
     * Thường được gọi tự động sau updateMeterReading(), nhưng
     * HOST cũng có thể gọi thủ công nếu cần recalculate.
     */
    @PostMapping("/calculate/{id}")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<InvoiceSummaryDTO>> calculateBill(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(billingService.calculateMonthlyBill(id)));
    }

    /**
     * Chủ trọ nhập chỉ số điện nước thực tế cho hóa đơn (Mục 2.4).
     *
     * Thứ tự bắt buộc:
     *   1. Gọi endpoint này để nhập chỉ số → tự động tính tổng tiền
     *   2. Sau đó gọi /pay khi thu được tiền
     *
     * Body: { "elecOld": 120, "elecNew": 145, "waterOld": 30, "waterNew": 34 }
     */
    @PutMapping("/{id}/meters")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<InvoiceSummaryDTO>> updateMeterReading(
            @PathVariable Long id,
            @RequestBody MeterReadingDTO dto) {
        InvoiceSummaryDTO result = billingService.updateMeterReading(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success("Đã cập nhật chỉ số và tính lại hóa đơn", result));
    }

    /**
     * Chủ trọ xác nhận đã thu được tiền từ người thuê (Mục 2.7).
     * Đổi trạng thái UNPAID/OVERDUE → PAID.
     *
     * Trong tương lai: tích hợp VNPay/MoMo để tự động confirm.
     */
    @PatchMapping("/{id}/pay")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<InvoiceSummaryDTO>> confirmPayment(
            @PathVariable Long id) {
        InvoiceSummaryDTO result = billingService.confirmPayment(id);
        return ResponseEntity.ok(
                ApiResponse.success("Xác nhận thanh toán thành công", result));
    }

    /**
     * Chủ trọ xem danh sách tất cả hóa đơn chưa thu được tiền (Mục 2.8).
     * OVERDUE hiển thị trước, sau đó UNPAID.
     */
    @GetMapping("/debts")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<List<InvoiceSummaryDTO>>> getDebtList() {
        return ResponseEntity.ok(
                ApiResponse.success(billingService.getDebtList()));
    }
}