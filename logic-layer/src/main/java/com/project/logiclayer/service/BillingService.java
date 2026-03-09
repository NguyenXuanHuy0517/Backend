package com.project.logiclayer.service;

import com.project.datalayer.dto.InvoiceSummaryDTO;
import com.project.datalayer.entity.Invoice;
import com.project.datalayer.mapper.InvoiceMapper;
import com.project.datalayer.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BillingService: Xử lý logic tính toán tài chính phức tạp.
 */
@Service
public class BillingService {

    @Autowired
    private InvoiceRepository invoiceRepository; // Từ Data Module

    @Autowired
    private InvoiceMapper invoiceMapper; // Từ Data Module

    /**
     * Lấy danh sách hóa đơn của một người thuê cụ thể.
     */
    public List<InvoiceSummaryDTO> getInvoicesByTenant(Long tenantId) {
        // Logic: Tìm hóa đơn thông qua quan hệ Contract -> Tenant
        return invoiceRepository.findAll().stream()
                .filter(inv -> inv.getContract().getTenant().getId().equals(tenantId))
                .map(invoiceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Logic tính tổng tiền hóa đơn: (Điện + Nước + Dịch vụ + Tiền phòng).
     * Hàm này thể hiện Business Logic mà Data Module không có.
     */
    @Transactional
    public InvoiceSummaryDTO calculateMonthlyBill(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // 1. Tính tiền điện: (Mới - Cũ) * Đơn giá
        BigDecimal elecCost = BigDecimal.valueOf(invoice.getElecNew() - invoice.getElecOld())
                .multiply(invoice.getContract().getRoom().getElecPrice());

        // 2. Tính tiền nước: (Mới - Cũ) * Đơn giá
        BigDecimal waterCost = BigDecimal.valueOf(invoice.getWaterNew() - invoice.getWaterOld())
                .multiply(invoice.getContract().getRoom().getWaterPrice());

        // 3. Cộng tiền phòng cố định và tiền dịch vụ (đã có trong invoice.getServiceFees())
        BigDecimal roomPrice = invoice.getContract().getActualRentPrice();
        BigDecimal total = roomPrice.add(elecCost).add(waterCost).add(invoice.getServiceFees());

        // 4. Cập nhật lại vào Database
        invoice.setTotalAmount(total);
        invoiceRepository.save(invoice);

        return invoiceMapper.toSummaryDTO(invoice);
    }
}