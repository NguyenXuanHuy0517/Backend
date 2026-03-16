package com.project.logiclayer.service;

import com.project.datalayer.dto.ServiceDTO;
import com.project.datalayer.entity.MotelArea;
import com.project.datalayer.entity.Service;
import com.project.datalayer.mapper.ServiceMapper;
import com.project.datalayer.repository.AreaRepository;
import com.project.datalayer.repository.ServiceRepository;
import com.project.logiclayer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ServiceBusinessService: Xử lý nghiệp vụ dịch vụ khu trọ (Mục 2.5).
 *
 * Lưu ý: Đặt tên là ServiceBusinessService để tránh xung đột với
 * java.util.Service hoặc Spring's @Service annotation. Tên class
 * không được trùng với tên annotation.
 */
@org.springframework.stereotype.Service
public class ServiceBusinessService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private ServiceMapper serviceMapper;

    /**
     * Lấy tất cả dịch vụ đang hoạt động.
     */
    public List<ServiceDTO> getAllActiveServices() {
        return serviceRepository.findByIsActiveTrue().stream()
                .map(serviceMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy dịch vụ theo khu trọ — mỗi khu có thể có dịch vụ và giá riêng.
     */
    public List<ServiceDTO> getServicesByArea(Long areaId) {
        return serviceRepository.findAll().stream()
                .filter(s -> s.getArea() != null && s.getArea().getId().equals(areaId))
                .map(serviceMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo dịch vụ mới cho một khu trọ.
     */
    @Transactional
    public ServiceDTO createService(ServiceDTO dto) {
        MotelArea area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khu trọ", "ID", dto.getAreaId()));

        Service service = new Service();
        service.setArea(area);
        service.setServiceName(dto.getServiceName());
        service.setPrice(dto.getPrice());
        service.setUnitName(dto.getUnitName() != null ? dto.getUnitName() : "Tháng");
        service.setDescription(dto.getDescription());

        return serviceMapper.toDTO(serviceRepository.save(service));
    }

    /**
     * Cập nhật thông tin dịch vụ (tên, giá, đơn vị tính, mô tả).
     */
    @Transactional
    public ServiceDTO updateService(Long serviceId, ServiceDTO dto) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ", "ID", serviceId));

        serviceMapper.updateEntityFromDTO(service, dto);
        return serviceMapper.toDTO(serviceRepository.save(service));
    }

    /**
     * Xóa dịch vụ.
     *
     * Kiểm tra an toàn: Nếu dịch vụ đang được dùng trong hợp đồng nào đó,
     * không cho phép xóa vật lý — ném exception để thông báo cho người dùng.
     * Trong thực tế có thể thêm cờ is_active = false thay vì xóa hẳn.
     */
    @Transactional
    public void deleteService(Long serviceId) {
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ", "ID", serviceId));

        // Kiểm tra xem dịch vụ có đang được dùng trong hợp đồng nào không
        boolean isInUse = service.getContractServices() != null
                && !service.getContractServices().isEmpty();

        if (isInUse) {
            throw new IllegalArgumentException(
                "Không thể xóa dịch vụ đang được sử dụng trong " +
                service.getContractServices().size() + " hợp đồng. " +
                "Hãy hủy đăng ký dịch vụ khỏi các hợp đồng trước.");
        }

        serviceRepository.delete(service);
    }
}
