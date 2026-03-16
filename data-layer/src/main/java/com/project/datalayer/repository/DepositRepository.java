package com.project.datalayer.repository;

import com.project.datalayer.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * DepositRepository: Truy vấn dữ liệu cọc tiền.
 */
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    /**
     * Lấy tất cả cọc của một người thuê (để hiển thị lịch sử đặt cọc).
     */
    List<Deposit> findByTenantId(Long tenantId);

    /**
     * Lấy tất cả cọc đang chờ xác nhận theo phòng.
     * Dùng khi chủ trọ xem phòng nào có người đang chờ đặt cọc.
     */
    List<Deposit> findByRoomIdAndStatus(Long roomId, String status);
}