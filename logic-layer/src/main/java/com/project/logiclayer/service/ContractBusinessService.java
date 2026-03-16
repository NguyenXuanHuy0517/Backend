package com.project.logiclayer.service;

import com.project.datalayer.dto.ContractDetailDTO;
import com.project.datalayer.dto.ContractRequestDTO;
import com.project.datalayer.dto.ServiceDTO;
import com.project.datalayer.entity.*;
import com.project.datalayer.mapper.ServiceMapper;
import com.project.datalayer.repository.*;
import com.project.logiclayer.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ContractBusinessService (cập nhật hoàn chỉnh — Mục 2.6).
 *
 * CÁC THAY ĐỔI / BỔ SUNG SO VỚI FILE TRƯỚC:
 *
 * 1. getContractDetail()   — MỚI: xem chi tiết 1 hợp đồng
 * 2. getMyActiveContract() — MỚI: người thuê xem hợp đồng đang có hiệu lực
 * 3. getContractsByTenant()— MỚI: lịch sử hợp đồng của người thuê
 * 4. addService()          — MỚI: người thuê đăng ký thêm dịch vụ
 * 5. removeService()       — MỚI: người thuê hủy dịch vụ
 * 6. createNewContract()   — GIỮ NGUYÊN từ file trước, không đổi
 * 7. extendContract()      — GIỮ NGUYÊN
 * 8. terminateContract()   — GIỮ NGUYÊN
 */
@Service
public class ContractBusinessService {

    @Autowired private ContractRepository contractRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private DepositRepository depositRepository;
    @Autowired private ServiceMapper serviceMapper;

    // ─── 1. Tạo hợp đồng mới ─────────────────────────────────────────────────

    @Transactional
    public Contract createNewContract(ContractRequestDTO dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "ID", dto.getRoomId()));

        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())
                && !"DEPOSITED".equalsIgnoreCase(room.getStatus())) {
            throw new IllegalArgumentException(
                    "Phòng đang ở trạng thái '" + room.getStatus() + "', không thể tạo hợp đồng.");
        }

        User tenant = userRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Người thuê", "ID", dto.getTenantId()));

        Contract contract = new Contract();
        contract.setContractCode(dto.getContractCode());
        contract.setRoom(room);
        contract.setTenant(tenant);
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setActualRentPrice(dto.getActualRentPrice());
        contract.setStatus("ACTIVE");

        if (dto.getDepositId() != null) {
            depositRepository.findById(dto.getDepositId()).ifPresent(deposit -> {
                contract.setDeposit(deposit);
                deposit.setStatus("COMPLETED");
            });
        }

        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()) {
            Set<ContractService> contractServices = new HashSet<>();
            for (Long serviceId : dto.getServiceIds()) {
                com.project.datalayer.entity.Service svc =
                        serviceRepository.findById(serviceId)
                                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ", "ID", serviceId));
                ContractService cs = new ContractService();
                ContractServiceId csId = new ContractServiceId();
                csId.setServiceId(serviceId);
                cs.setId(csId);
                cs.setContract(contract);
                cs.setService(svc);
                cs.setQuantity(1);
                contractServices.add(cs);
            }
            contract.setContractServices(contractServices);
        }

        room.setStatus("RENTED");
        roomRepository.save(room);
        return contractRepository.save(contract);
    }

    // ─── 2. Xem chi tiết hợp đồng ────────────────────────────────────────────

    /**
     * Lấy chi tiết đầy đủ một hợp đồng theo ID.
     * Dùng cho cả HOST và TENANT (TENANT chỉ được xem hợp đồng của mình
     * — cần kiểm tra quyền ở Controller bằng @PreAuthorize).
     */
    public ContractDetailDTO getContractDetail(Long contractId) {
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng", "ID", contractId));
        return toDetailDTO(c);
    }

    /**
     * Người thuê xem hợp đồng ACTIVE hiện tại của mình.
     */
    public ContractDetailDTO getMyActiveContract(Long tenantId) {
        Contract c = contractRepository.findFirstByTenantIdAndStatus(tenantId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hợp đồng đang hiệu lực cho người thuê ID: " + tenantId));
        return toDetailDTO(c);
    }

    /**
     * Lịch sử tất cả hợp đồng của một người thuê.
     */
    public List<ContractDetailDTO> getContractsByTenant(Long tenantId) {
        return contractRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toDetailDTO).collect(Collectors.toList());
    }

    // ─── 3. Đăng ký / hủy dịch vụ (Mục 2.5) ─────────────────────────────────

    /**
     * Người thuê đăng ký thêm một dịch vụ vào hợp đồng đang ACTIVE.
     *
     * Khi thêm dịch vụ, serviceFees của các hóa đơn UNPAID tháng này
     * sẽ được cập nhật tự động.
     *
     * @param contractId ID hợp đồng
     * @param serviceId  ID dịch vụ muốn đăng ký
     */
    @Transactional
    public ContractDetailDTO addService(Long contractId, Long serviceId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng", "ID", contractId));

        if (!"ACTIVE".equals(contract.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể đăng ký dịch vụ cho hợp đồng đang ACTIVE.");
        }

        com.project.datalayer.entity.Service svc = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ", "ID", serviceId));

        // Kiểm tra đã đăng ký dịch vụ này chưa
        boolean alreadyRegistered = contract.getContractServices().stream()
                .anyMatch(cs -> cs.getService().getId().equals(serviceId));
        if (alreadyRegistered) {
            throw new IllegalArgumentException("Dịch vụ '" + svc.getServiceName() + "' đã được đăng ký trước đó.");
        }

        ContractService cs = new ContractService();
        ContractServiceId csId = new ContractServiceId();
        csId.setContractId(contractId);
        csId.setServiceId(serviceId);
        cs.setId(csId);
        cs.setContract(contract);
        cs.setService(svc);
        cs.setQuantity(1);
        contract.getContractServices().add(cs);

        contractRepository.save(contract);
        return toDetailDTO(contract);
    }

    /**
     * Người thuê hủy đăng ký một dịch vụ khỏi hợp đồng.
     *
     * @param contractId ID hợp đồng
     * @param serviceId  ID dịch vụ muốn hủy
     */
    @Transactional
    public ContractDetailDTO removeService(Long contractId, Long serviceId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng", "ID", contractId));

        if (!"ACTIVE".equals(contract.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hủy dịch vụ trên hợp đồng đang ACTIVE.");
        }

        boolean removed = contract.getContractServices()
                .removeIf(cs -> cs.getService().getId().equals(serviceId));

        if (!removed) {
            throw new ResourceNotFoundException("Dịch vụ ID " + serviceId + " không có trong hợp đồng này.");
        }

        contractRepository.save(contract);
        return toDetailDTO(contract);
    }

    // ─── 4. Gia hạn & chấm dứt ───────────────────────────────────────────────

    @Transactional
    public void extendContract(Long contractId, LocalDate newEndDate) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng", "ID", contractId));
        if (!newEndDate.isAfter(contract.getEndDate())) {
            throw new IllegalArgumentException(
                    "Ngày gia hạn phải sau ngày kết thúc hiện tại: " + contract.getEndDate());
        }
        contract.setEndDate(newEndDate);
        contractRepository.save(contract);
    }

    @Transactional
    public void terminateContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Hợp đồng", "ID", contractId));
        if (!"ACTIVE".equals(contract.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể chấm dứt hợp đồng đang ACTIVE. Trạng thái: " + contract.getStatus());
        }
        contract.setStatus("TERMINATED_EARLY");
        if (contract.getRoom() != null) {
            contract.getRoom().setStatus("AVAILABLE");
            roomRepository.save(contract.getRoom());
        }
        contractRepository.save(contract);
    }

    // ─── Private helper ───────────────────────────────────────────────────────

    private ContractDetailDTO toDetailDTO(Contract c) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), c.getEndDate());

        List<ServiceDTO> services = c.getContractServices() != null
                ? c.getContractServices().stream()
                .map(cs -> serviceMapper.toDTO(cs.getService()))
                .collect(Collectors.toList())
                : List.of();

        return ContractDetailDTO.builder()
                .contractId(c.getId())
                .contractCode(c.getContractCode())
                .roomId(c.getRoom() != null ? c.getRoom().getId() : null)
                .roomCode(c.getRoom() != null ? c.getRoom().getRoomCode() : null)
                .areaName(c.getRoom() != null && c.getRoom().getArea() != null
                        ? c.getRoom().getArea().getAreaName() : null)
                .address(c.getRoom() != null && c.getRoom().getArea() != null
                        ? c.getRoom().getArea().getAddress() : null)
                .tenantId(c.getTenant() != null ? c.getTenant().getId() : null)
                .tenantName(c.getTenant() != null ? c.getTenant().getFullName() : null)
                .tenantPhone(c.getTenant() != null ? c.getTenant().getPhoneNumber() : null)
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .daysUntilExpiry(daysLeft)
                .actualRentPrice(c.getActualRentPrice())
                .status(c.getStatus())
                .registeredServices(services)
                .depositId(c.getDeposit() != null ? c.getDeposit().getId() : null)
                .depositAmount(c.getDeposit() != null ? c.getDeposit().getAmount() : null)
                .createdAt(c.getCreatedAt())
                .build();
    }
}