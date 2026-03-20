package com.project.logiclayer.service;

import com.project.datalayer.dto.TenantDTO;
import com.project.datalayer.dto.UserDTO;
import com.project.datalayer.entity.Contract;
import com.project.datalayer.entity.MotelArea;
import com.project.datalayer.entity.Room;
import com.project.datalayer.entity.User;
import com.project.datalayer.repository.AreaRepository;
import com.project.datalayer.repository.ContractRepository;
import com.project.datalayer.repository.RoleRepository;
import com.project.datalayer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TenantService: Quản lý người thuê thuộc về một Host cụ thể.
 *
 * Logic xác định "tenant của host":
 *   Host → MotelArea → Room → Contract → Tenant
 * Tức là tenant nào có hợp đồng trong phòng thuộc khu trọ của host đó.
 */
@Service
public class TenantService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Lấy danh sách tenant của một host ────────────────────────────────────

    /**
     * Lấy tất cả tenant có hợp đồng ACTIVE trong các phòng thuộc khu trọ của host.
     * Trả về TenantDTO kèm thông tin hợp đồng và phòng hiện tại.
     */
    public List<TenantDTO> getTenantsOfHost(Long hostId) {
        // Lấy tất cả khu trọ của host
        List<MotelArea> areas = areaRepository.findByHostId(hostId);

        // Lấy tất cả roomId thuộc host
        List<Long> roomIds = areas.stream()
                .flatMap(area -> area.getRooms().stream())
                .map(Room::getId)
                .collect(Collectors.toList());

        if (roomIds.isEmpty()) return List.of();

        // Lấy tất cả hợp đồng trong các phòng đó, ưu tiên ACTIVE
        List<Contract> contracts = contractRepository.findByRoomIdIn(roomIds);

        // Gom nhóm theo tenant, lấy hợp đồng mới nhất cho mỗi tenant
        return contracts.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getTenant().getId(),
                        Collectors.maxBy(Comparator.comparing(Contract::getId))
                ))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toTenantDTO)
                .sorted(Comparator.comparing(t -> t.getFullName() != null ? t.getFullName() : ""))
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một tenant theo userId.
     */
    public TenantDTO getTenantDetail(Long tenantId) {
        User user = userRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê với ID: " + tenantId));

        // Tìm hợp đồng ACTIVE mới nhất
        List<Contract> contracts = contractRepository.findByTenantId(tenantId);
        Optional<Contract> activeContract = contracts.stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .max(Comparator.comparing(Contract::getId));

        if (activeContract.isPresent()) {
            return toTenantDTO(activeContract.get());
        }

        // Không có hợp đồng → trả về thông tin user cơ bản
        return TenantDTO.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .idCardNumber(user.getIdCardNumber())
                .isActive(user.getIsActive())
                .build();
    }

    // ── Tạo tenant mới ────────────────────────────────────────────────────────

    /**
     * Host tạo tài khoản tenant mới (không cần tenant tự đăng ký).
     * Password mặc định = số điện thoại.
     */
    @Transactional
    public TenantDTO createTenant(UserDTO dto) {
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại '" + dto.getPhoneNumber() + "' đã được đăng ký.");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setIdCardNumber(dto.getIdCardNumber());
        user.setIsActive(true);
        // Password mặc định = số điện thoại (tenant đổi sau)
        user.setPasswordHash(passwordEncoder.encode(dto.getPhoneNumber()));
        roleRepository.findByRoleName("TENANT").ifPresent(user::setRole);

        User saved = userRepository.save(user);

        return TenantDTO.builder()
                .userId(saved.getId())
                .fullName(saved.getFullName())
                .phoneNumber(saved.getPhoneNumber())
                .email(saved.getEmail())
                .idCardNumber(saved.getIdCardNumber())
                .isActive(saved.getIsActive())
                .build();
    }

    // ── Cập nhật tenant ───────────────────────────────────────────────────────

    /**
     * Cập nhật thông tin cơ bản của tenant (fullName, email, idCardNumber).
     */
    @Transactional
    public TenantDTO updateTenant(Long tenantId, UserDTO dto) {
        User user = userRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê"));

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());

        // FIX: Chỉ update email nếu thực sự thay đổi so với email hiện tại
        // Tránh lỗi Duplicate entry khi Flutter gửi lại email cũ
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            // Kiểm tra email mới có bị trùng với user khác không
            userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(tenantId)) {
                    throw new RuntimeException("Email '" + dto.getEmail() + "' đã được sử dụng bởi tài khoản khác");
                }
            });
            user.setEmail(dto.getEmail());
        }

        if (dto.getIdCardNumber() != null) user.setIdCardNumber(dto.getIdCardNumber());

        userRepository.save(user);
        return getTenantDetail(tenantId);
    }

    /**
     * Khoá / mở khoá tài khoản tenant.
     */
    @Transactional
    public TenantDTO toggleActive(Long tenantId) {
        User user = userRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê"));
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        userRepository.save(user);
        return getTenantDetail(tenantId);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private TenantDTO toTenantDTO(Contract contract) {
        User tenant = contract.getTenant();
        Room room = contract.getRoom();
        return TenantDTO.builder()
                .userId(tenant.getId())
                .fullName(tenant.getFullName())
                .phoneNumber(tenant.getPhoneNumber())
                .email(tenant.getEmail())
                .idCardNumber(tenant.getIdCardNumber())
                .isActive(tenant.getIsActive())
                // Contract
                .contractId(contract.getId())
                .contractCode(contract.getContractCode())
                .contractStatus(contract.getStatus())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .actualRentPrice(contract.getActualRentPrice())
                // Room
                .roomId(room != null ? room.getId() : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .areaName(room != null && room.getArea() != null ? room.getArea().getAreaName() : null)
                .build();
    }
}