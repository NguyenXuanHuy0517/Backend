package com.project.datalayer.mapper;

import com.project.cruddata.entity.Invoice;
import com.project.cruddata.dto.InvoiceSummaryDTO;
import com.project.cruddata.dto.InvoiceServiceDetailDTO;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * InvoiceMapper: Chuyển đổi hóa đơn và các chi tiết dịch vụ đi kèm.
 */
@Component
public class InvoiceMapper {

    /**
     * Chuyển đổi từ Invoice Entity sang InvoiceSummaryDTO.
     */
    public InvoiceSummaryDTO toSummaryDTO(Invoice entity) {
        if (entity == null) return null;

        return InvoiceSummaryDTO.builder()
                .invoiceId(entity.getId())
                .invoiceCode(entity.getInvoiceCode())
                .billingMonth(entity.getBillingMonth())
                .billingYear(entity.getBillingYear())
                .elecOld(entity.getElecOld())
                .elecNew(entity.getElecNew())
                .waterOld(entity.getWaterOld())
                .waterNew(entity.getWaterNew())
                .serviceFees(entity.getServiceFees())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                // Map danh sách dịch vụ từ hợp đồng (Contract) liên quan đến hóa đơn này
                .serviceDetails(entity.getContract() != null && entity.getContract().getContractServices() != null
                        ? entity.getContract().getContractServices().stream()
                        .map(cs -> InvoiceServiceDetailDTO.builder()
                                .serviceName(cs.getService().getServiceName())
                                .servicePrice(cs.getService().getPrice())
                                .quantity(cs.getQuantity())
                                // Tính thành tiền cho từng dịch vụ: Price * Quantity
                                .subTotal(cs.getService().getPrice().multiply(BigDecimal.valueOf(cs.getQuantity())))
                                .build())
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }
}