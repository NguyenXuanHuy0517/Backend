package com.project.logiclayer.service;

import com.project.datalayer.dto.ContractRequestDTO;
import com.project.datalayer.entity.Contract;
import com.project.datalayer.entity.ContractService;
import com.project.datalayer.entity.Room;
import com.project.datalayer.entity.User;
import com.project.datalayer.entity.Deposit;
import com.project.datalayer.repository.ContractRepository;
import com.project.datalayer.repository.RoomRepository;
import com.project.datalayer.repository.UserRepository;
import com.project.datalayer.repository.ServiceRepository; // Giả định ServiceRepository tồn tại trong data-layer
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Xử lý nghiệp vụ Hợp đồng thuê (Mục 2.6)
 * Đã cập nhật khớp với ContractRequestDTO mới.
 */
@Service
public class ContractBusinessService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Tạo hợp đồng mới dựa trên DTO gửi từ App.
     * Xử lý: Gán phòng, người thuê, tiền cọc và danh sách dịch vụ đăng ký.
     */
    @Transactional
    public Contract createNewContract(ContractRequestDTO dto) {
        // 1. Kiểm tra sự tồn tại của Phòng
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // 2. Kiểm tra trạng thái phòng (Chỉ phòng AVAILABLE mới được thuê)
        if (room.getStatus() == null || !"AVAILABLE".equalsIgnoreCase(room.getStatus().name())) {
            throw new RuntimeException("Phòng này hiện đang ở trạng thái: " +
                    (room.getStatus() != null ? room.getStatus().name() : "UNKNOWN") + " và không thể cho thuê");
        }

        // 3. Kiểm tra Người thuê (Tenant)
        User tenant = userRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new RuntimeException("Người thuê không tồn tại"));

        // 4. Khởi tạo Contract Entity
        Contract contract = new Contract();
        contract.setContractCode(dto.getContractCode());
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setActualRentPrice(dto.getActualRentPrice());
        contract.setStatus("ACTIVE");

        // 5. Liên kết tiền cọc nếu có (depositId)
        if (dto.getDepositId() != null) {
            // Giả định bạn có logic xử lý Deposit ở đây hoặc thiết lập trực tiếp Entity
            // contract.setDeposit(depositRepository.findById(dto.getDepositId()).orElse(null));
        }

        // 6. Xử lý danh sách dịch vụ đăng ký (Mục 2.5)
        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()) {
            // Chuyển đổi ID dịch vụ thành danh sách ContractService (quan hệ trung gian)
            List<ContractService> contractServices = dto.getServiceIds().stream().map(serviceId -> {
                com.project.datalayer.entity.Service serviceEntity = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new RuntimeException("Dịch vụ ID " + serviceId + " không tồn tại"));

                ContractService cs = new ContractService();
                cs.setContract(contract);
                cs.setService(serviceEntity);
                cs.setQuantity(1); // Mặc định số lượng là 1 khi bắt đầu hợp đồng
                return cs;
            }).collect(Collectors.toList());

            contract.setContractServices(contractServices);
        }

        // 7. Cập nhật trạng thái phòng sang RENTED
        room.setStatus(com.project.datalayer.entity.RoomStatus.RENTED); // Giả định dùng Enum RoomStatus
        roomRepository.save(room);

        return contractRepository.save(contract);
    }

    /**
     * Gia hạn hợp đồng (Mục 2.6)
     */
    @Transactional
    public void extendContract(Long contractId, LocalDate newEndDate) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Hợp đồng không tồn tại"));

        if (newEndDate.isBefore(contract.getEndDate())) {
            throw new RuntimeException("Ngày kết thúc mới không được trước ngày kết thúc cũ");
        }

        contract.setEndDate(newEndDate);
        contractRepository.save(contract);
    }
}