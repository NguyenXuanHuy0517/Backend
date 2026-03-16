package com.project.logiclayer.controller;

import com.project.datalayer.dto.ApiResponse;
import com.project.datalayer.dto.InvoiceSummaryDTO;
import com.project.datalayer.dto.MeterReadingDTO;
import com.project.logiclayer.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private BillingService billingService;

    /**
     * Người thuê xem danh sách hóa đơn của mình, mới nhất trước.
     */
    @GetMapping("/my-bills/{tenantId}")
    public ResponseEntity<ApiResponse<List<InvoiceSummaryDTO>>> getMyInvoices(
            @PathVariable Long tenantId) {
        logger.info("[INVOICE] GET /api/business/invoices/my-bills/{} - Fetching invoices for tenant", tenantId);
        try {
            List<InvoiceSummaryDTO> invoices = billingService.getInvoicesByTenant(tenantId);
            logger.info("[INVOICE] GET /api/business/invoices/my-bills/{} - Retrieved {} invoices", tenantId, invoices.size());
            return ResponseEntity.ok(ApiResponse.success(invoices));
        } catch (Exception e) {
            logger.error("[INVOICE] GET /api/business/invoices/my-bills/{} - Error: {}", tenantId, e.getMessage(), e);
            throw e;
        }
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
        logger.info("[INVOICE] POST /api/business/invoices/calculate/{} - Calculating bill", id);
        try {
            InvoiceSummaryDTO result = billingService.calculateMonthlyBill(id);
            logger.info("[INVOICE] POST /api/business/invoices/calculate/{} - Bill calculated successfully", id);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("[INVOICE] POST /api/business/invoices/calculate/{} - Error: {}", id, e.getMessage(), e);
            throw e;
        }
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
        logger.info("[INVOICE] PUT /api/business/invoices/{}/meters - Updating meter reading. elecNew: {}, waterNew: {}",
                id, dto.getElecNew(), dto.getWaterNew());
        try {
            InvoiceSummaryDTO result = billingService.updateMeterReading(id, dto);
            logger.info("[INVOICE] PUT /api/business/invoices/{}/meters - Meter updated and bill recalculated", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Đã cập nhật chỉ số và tính lại hóa đơn", result));
        } catch (Exception e) {
            logger.error("[INVOICE] PUT /api/business/invoices/{}/meters - Error: {}", id, e.getMessage(), e);
            throw e;
        }
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
        logger.info("[INVOICE] PATCH /api/business/invoices/{}/pay - Confirming payment", id);
        try {
            InvoiceSummaryDTO result = billingService.confirmPayment(id);
            logger.info("[INVOICE] PATCH /api/business/invoices/{}/pay - Payment confirmed successfully", id);
            return ResponseEntity.ok(
                    ApiResponse.success("Xác nhận thanh toán thành công", result));
        } catch (Exception e) {
            logger.error("[INVOICE] PATCH /api/business/invoices/{}/pay - Error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Chủ trọ xem danh sách tất cả hóa đơn chưa thu được tiền (Mục 2.8).
     * OVERDUE hiển thị trước, sau đó UNPAID.
     */
    @GetMapping("/debts")
    // @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<List<InvoiceSummaryDTO>>> getDebtList() {
        logger.info("[INVOICE] GET /api/business/invoices/debts - Fetching debt list");
        try {
            List<InvoiceSummaryDTO> debts = billingService.getDebtList();
            logger.info("[INVOICE] GET /api/business/invoices/debts - Retrieved {} debt items", debts.size());
            return ResponseEntity.ok(ApiResponse.success(debts));
        } catch (Exception e) {
            logger.error("[INVOICE] GET /api/business/invoices/debts - Error: {}", e.getMessage(), e);
            throw e;
        }
    }
}