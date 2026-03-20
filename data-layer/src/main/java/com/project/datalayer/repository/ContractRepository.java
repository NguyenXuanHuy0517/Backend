package com.project.datalayer.repository;

import com.project.datalayer.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ContractRepository — Fix đủ method.
 *
 * LỖI ĐÃ SỬA:
 * "cannot find symbol method findFirstByTenantIdAndStatus(Long, String)"    — ContractService :117
 * "cannot find symbol method findByTenantIdOrderByCreatedAtDesc(Long)"      — ContractService :127
 *
 * Cần copy file này vào:
 *   data-layer/src/main/java/com/project/datalayer/repository/ContractRepository.java
 */
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /**
     * Tìm hợp đồng ACTIVE đầu tiên của người thuê.
     * ContractBusinessService.getMyActiveContract() dùng method này.
     * Một người thuê chỉ nên có 1 ACTIVE tại một thời điểm.
     */
    Optional<Contract> findFirstByTenant_IdAndStatus(Long tenantId, String status);

    /**
     * Lịch sử tất cả hợp đồng của người thuê, mới nhất trước.
     * ContractBusinessService.getContractsByTenant() dùng method này.
     */
    List<Contract> findByTenant_IdOrderByCreatedAtDesc(Long tenantId);

    /**
     * Lấy tất cả hợp đồng theo trạng thái.
     * NotificationScheduler và ReportService dùng để lọc ACTIVE contracts.
     */
    List<Contract> findByStatus(String status);

    /**
     * Đếm hợp đồng ACTIVE theo khu trọ.
     * ReportService.getOccupancyByArea() dùng để tính tỷ lệ lấp đầy từng khu.
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.room.area.id = :areaId AND c.status = 'ACTIVE'")
    long countActiveContractsByAreaId(@Param("areaId") Long areaId);

    // Lấy tất cả hợp đồng trong danh sách roomId
    List<Contract> findByRoomIdIn(List<Long> roomIds);

    // Lấy tất cả hợp đồng của một tenant
    List<Contract> findByTenantId(Long tenantId);
}