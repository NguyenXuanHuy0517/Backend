package com.project.logiclayer.service;

import com.project.datalayer.dto.InvoiceSummaryDTO;
import com.project.datalayer.dto.MeterReadingDTO;
import com.project.datalayer.entity.Contract;
import com.project.datalayer.entity.Invoice;
import com.project.datalayer.mapper.InvoiceMapper;
import com.project.datalayer.repository.InvoiceRepository;
import com.project.logiclayer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BillingService (cập nhật hoàn chỉnh — Mục 2.4 + 2.7).
 *
 * CÁC THAY ĐỔI SO VỚI FILE GỐC:
 *
 * 1. updateMeterReading() — MỚI
 *    Chủ trọ nhập chỉ số điện nước thực tế vào hóa đơn đã tạo.
 *    Sau khi nhập, tự động tính lại tổng tiền.
 *
 * 2. confirmPayment() — MỚI
 *    Chủ trọ xác nhận đã thu được tiền → đổi status UNPAID/OVERDUE → PAID.
 *
 * 3. getDebtList() — MỚI
 *    Chủ trọ xem danh sách tất cả hóa đơn chưa thanh toán để theo dõi nợ.
 *
 * 4. getInvoicesByTenant() — SỬA
 *    Dùng query method mới trong InvoiceRepository thay vì stream().filter()
 *    để tránh load toàn bộ bảng invoices mỗi lần gọi.
 *
 * 5. calculateMonthlyBill() — GIỮ NGUYÊN LOGIC, chỉ đổi exception type
 *    Dùng ResourceNotFoundException thay RuntimeException.
 */
@Service
public class BillingService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceMapper invoiceMapper;

    // ─── 1. Lấy hóa đơn của người thuê ───────────────────────────────────────

    /**
     * Lấy tất cả hóa đơn của một người thuê, mới nhất trước.
     * Sử dụng @Query trong InvoiceRepository thay vì stream().filter()
     * để tránh load toàn bộ bảng vào memory.
     */
    public List<InvoiceSummaryDTO> getInvoicesByTenant(Long tenantId) {
        return invoiceRepository.findByTenantId(tenantId).stream()
                .map(invoiceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ─── 2. Nhập chỉ số điện nước (MỤC 2.4) ─────────────────────────────────

    /**
     * Chủ trọ nhập chỉ số điện nước thực tế cho hóa đơn.
     *
     * Luồng:
     *   - Đầu tháng: InvoiceScheduler tạo hóa đơn với elecOld/New = 0
     *   - Chủ trọ đi đọc đồng hồ → gọi API này để cập nhật số thực
     *   - Method tự động gọi lại calculateMonthlyBill() để ra tổng tiền
     *
     * Validation:
     *   - Chỉ số mới phải >= chỉ số cũ
     *   - Chỉ được nhập khi hóa đơn còn UNPAID (không sửa hóa đơn đã thu)
     *
     * @param invoiceId ID hóa đơn cần cập nhật
     * @param dto       Chỉ số điện/nước cũ và mới
     */
    @Transactional
    public InvoiceSummaryDTO updateMeterReading(Long invoiceId, MeterReadingDTO dto) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "ID", invoiceId));

        // Không cho phép sửa hóa đơn đã thanh toán
        if ("PAID".equals(invoice.getStatus())) {
            throw new IllegalArgumentException(
                    "Hóa đơn đã được thanh toán, không thể chỉnh sửa chỉ số.");
        }

        // Validate: chỉ số mới >= chỉ số cũ
        if (dto.getElecNew() < dto.getElecOld()) {
            throw new IllegalArgumentException(
                    "Chỉ số điện mới (" + dto.getElecNew() + ") không được nhỏ hơn chỉ số cũ (" + dto.getElecOld() + ").");
        }
        if (dto.getWaterNew() < dto.getWaterOld()) {
            throw new IllegalArgumentException(
                    "Chỉ số nước mới (" + dto.getWaterNew() + ") không được nhỏ hơn chỉ số cũ (" + dto.getWaterOld() + ").");
        }

        // Cập nhật chỉ số
        invoice.setElecOld(dto.getElecOld());
        invoice.setElecNew(dto.getElecNew());
        invoice.setWaterOld(dto.getWaterOld());
        invoice.setWaterNew(dto.getWaterNew());
        invoiceRepository.save(invoice);

        // Tự động tính lại tổng tiền sau khi nhập chỉ số
        return calculateMonthlyBill(invoiceId);
    }

    // ─── 3. Tính tổng tiền hóa đơn (MỤC 2.4 + 2.7) ──────────────────────────

    /**
     * Tính tổng tiền hóa đơn dựa trên: tiền phòng + điện + nước + dịch vụ.
     *
     * Công thức:
     *   elecCost  = (elecNew - elecOld) × elecPrice/kWh của phòng
     *   waterCost = (waterNew - waterOld) × waterPrice/m³ của phòng
     *   total     = actualRentPrice + elecCost + waterCost + serviceFees
     *
     * Lưu ý serviceFees: Giá trị này được tính sẵn khi tạo hóa đơn dựa trên
     * các dịch vụ đã đăng ký trong hợp đồng. Xem InvoiceScheduler.
     */
    @Transactional
    public InvoiceSummaryDTO calculateMonthlyBill(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "ID", invoiceId));

        Contract contract = invoice.getContract();

        // Tính tiền điện: (chỉ số mới - cũ) × đơn giá điện của phòng
        BigDecimal elecCost = BigDecimal
                .valueOf(invoice.getElecNew() - invoice.getElecOld())
                .multiply(contract.getRoom().getElecPrice());

        // Tính tiền nước: (chỉ số mới - cũ) × đơn giá nước của phòng
        BigDecimal waterCost = BigDecimal
                .valueOf(invoice.getWaterNew() - invoice.getWaterOld())
                .multiply(contract.getRoom().getWaterPrice());

        // Tổng = tiền phòng + điện + nước + dịch vụ
        BigDecimal total = contract.getActualRentPrice()
                .add(elecCost)
                .add(waterCost)
                .add(invoice.getServiceFees() != null ? invoice.getServiceFees() : BigDecimal.ZERO);

        invoice.setTotalAmount(total);
        invoiceRepository.save(invoice);

        return invoiceMapper.toSummaryDTO(invoice);
    }

    // ─── 4. Xác nhận thanh toán (MỤC 2.7) ────────────────────────────────────

    /**
     * Chủ trọ xác nhận đã thu tiền từ người thuê → đổi status sang PAID.
     *
     * Trong phiên bản nâng cao, đây là nơi tích hợp với cổng thanh toán
     * (VNPay, MoMo...) để xác nhận tự động. Hiện tại là xác nhận thủ công.
     *
     * @param invoiceId ID hóa đơn cần xác nhận
     */
    @Transactional
    public InvoiceSummaryDTO confirmPayment(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "ID", invoiceId));

        if ("PAID".equals(invoice.getStatus())) {
            throw new IllegalArgumentException("Hóa đơn này đã được thanh toán trước đó.");
        }

        // Kiểm tra tổng tiền đã được tính chưa (chỉ số điện nước đã nhập chưa)
        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "Chưa nhập chỉ số điện nước cho hóa đơn này. " +
                            "Vui lòng cập nhật chỉ số trước khi xác nhận thanh toán.");
        }

        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        return invoiceMapper.toSummaryDTO(invoice);
    }

    // ─── 5. Xem danh sách nợ (MỤC 2.8) ─────────────────────────────────────

    /**
     * Chủ trọ xem danh sách tất cả hóa đơn chưa thanh toán.
     * Bao gồm cả UNPAID lẫn OVERDUE, sắp xếp để nợ lâu nhất lên đầu.
     */
    public List<InvoiceSummaryDTO> getDebtList() {
        List<Invoice> unpaid = invoiceRepository.findByStatus("UNPAID");
        List<Invoice> overdue = invoiceRepository.findByStatus("OVERDUE");

        // Gộp 2 danh sách, OVERDUE lên trước (nặng hơn)
        return java.util.stream.Stream.concat(overdue.stream(), unpaid.stream())
                .map(invoiceMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }
}