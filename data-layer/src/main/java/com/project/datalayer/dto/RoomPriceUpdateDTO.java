package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * RoomPriceUpdateDTO: Cập nhật giá điện nước riêng cho một phòng (Mục 2.3).
 *
 * Theo yêu cầu Mục 2.3:
 * - Giá điện nước mặc định lưu trong bảng rooms (elec_price, water_price)
 * - Có thể thiết lập giá riêng theo từng phòng thông qua DTO này
 * - BillingService luôn dùng giá của phòng khi tính hóa đơn
 *
 * Nếu muốn giá riêng theo hợp đồng (không phải theo phòng), sẽ cần
 * thêm cột elec_price/water_price vào bảng contracts — có thể làm ở bước sau.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomPriceUpdateDTO {

    /**
     * Giá điện (đồng/kWh).
     * Null = giữ nguyên giá hiện tại.
     * Ví dụ: 3500 (3.500đ/kWh theo giá EVN phổ biến)
     */
    private BigDecimal elecPrice;

    /**
     * Giá nước (đồng/m³).
     * Null = giữ nguyên giá hiện tại.
     * Ví dụ: 15000 (15.000đ/m³)
     */
    private BigDecimal waterPrice;

    /**
     * Lý do thay đổi giá (để ghi vào lịch sử, tùy chọn).
     * Ví dụ: "Tăng giá điện theo thông báo EVN tháng 5/2024"
     */
    private String reason;
}