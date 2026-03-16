package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * ServiceDTO: Thông tin dịch vụ trả về cho Flutter (Mục 2.5).
 *
 * Dùng để:
 * - Hiển thị danh sách dịch vụ để người thuê đăng ký
 * - Hiển thị đơn giá trong trang quản lý dịch vụ của chủ trọ
 *
 * KHÔNG trả về entity Service trực tiếp vì entity chứa quan hệ
 * contractServices (lazy) có thể gây LazyInitializationException.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {

    private Long serviceId;
    private String serviceName;
    private BigDecimal price;

    /**
     * Đơn vị tính phí: "Tháng" (mặc định), "Lượt", "Người", v.v.
     * Tương ứng với cột unit_name trong bảng services.
     */
    private String unitName;

    private String description;

    /**
     * ID khu trọ sở hữu dịch vụ này.
     * Mỗi khu trọ có thể có bộ dịch vụ và đơn giá riêng.
     */
    private Long areaId;

    private String areaName;

    /**
     * Trạng thái dịch vụ có đang hoạt động không.
     * Dùng khi chủ trọ muốn tạm ngưng một dịch vụ mà không xóa.
     *
     * Lưu ý: Cột is_active chưa có trong schema SQL hiện tại.
     * Nếu chưa muốn thêm cột, có thể bỏ trường này.
     */
    private Boolean isActive;
}