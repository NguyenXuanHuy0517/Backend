package com.project.logiclayer.controller;

import com.project.logiclayer.service.BillingService;
import com.project.datalayer.dto.InvoiceSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InvoiceController: Cung cấp các Endpoint về tài chính cho App Flutter.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/business/invoices")
public class InvoiceController {

    @Autowired
    private BillingService billingService;

    /**
     * API: Lấy danh sách hóa đơn của tôi (Dành cho Tenant)
     */
    @GetMapping("/my-bills/{tenantId}")
    public ResponseEntity<List<InvoiceSummaryDTO>> getMyInvoices(@PathVariable Long tenantId) {
        return ResponseEntity.ok(billingService.getInvoicesByTenant(tenantId));
    }

    /**
     * API: Yêu cầu tính toán/chốt hóa đơn (Dành cho Host)
     */
    @PostMapping("/calculate/{id}")
    public ResponseEntity<InvoiceSummaryDTO> calculateBill(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.calculateMonthlyBill(id));
    }
}