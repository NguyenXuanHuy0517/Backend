package com.project.datalayer.mapper;

import com.project.datalayer.entity.Service;
import com.project.datalayer.dto.ServiceDTO;
import org.springframework.stereotype.Component;

/**
 * ServiceMapper: Chuyển đổi giữa Service entity và ServiceDTO.
 *
 * Lý do cần mapper thay vì trả entity trực tiếp:
 * Service entity có quan hệ @OneToMany với ContractService (lazy fetch).
 * Nếu trả entity ra ngoài transaction, truy cập contractServices sẽ
 * ném LazyInitializationException. Mapper chỉ lấy những trường cần thiết.
 */
@Component
public class ServiceMapper {

    public ServiceDTO toDTO(Service entity) {
        if (entity == null) return null;

        return ServiceDTO.builder()
                .serviceId(entity.getId())
                .serviceName(entity.getServiceName())
                .price(entity.getPrice())
                .unitName(entity.getUnitName())
                .description(entity.getDescription())
                // Lấy thông tin khu trọ từ quan hệ ManyToOne
                .areaId(entity.getArea() != null ? entity.getArea().getId() : null)
                .areaName(entity.getArea() != null ? entity.getArea().getAreaName() : null)
                // Service entity hiện tại chưa có cột is_active trong DB.
                // Mặc định true để không ảnh hưởng logic hiện tại.
                .isActive(true)
                .build();
    }

    /**
     * Cập nhật thông tin entity từ DTO (dùng khi chủ trọ chỉnh sửa dịch vụ).
     * Không cập nhật areaId vì một dịch vụ không thể chuyển sang khu trọ khác.
     */
    public void updateEntityFromDTO(Service entity, ServiceDTO dto) {
        if (dto.getServiceName() != null) entity.setServiceName(dto.getServiceName());
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        if (dto.getUnitName() != null) entity.setUnitName(dto.getUnitName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
    }
}