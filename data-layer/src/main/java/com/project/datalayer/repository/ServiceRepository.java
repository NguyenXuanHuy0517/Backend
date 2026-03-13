package com.project.datalayer.repository;

import com.project.datalayer.entity.Service;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ServiceRepository: Quản lý danh mục dịch vụ (Wifi, vệ sinh, rác...).
 * Mục 2.5: Quản lý dịch vụ và đơn giá.
 */
@Repository
public interface ServiceRepository extends BaseRepository<Service, Long> {

    /**
     * Tìm danh sách các dịch vụ đang hoạt động.
     */
    List<Service> findByIsActiveTrue();

    /**
     * Lấy danh sách dịch vụ theo một danh sách ID cụ thể.
     * Thường dùng để kiểm tra tính hợp lệ khi người thuê đăng ký dịch vụ.
     */
    List<Service> findByServiceIdIn(List<Long> serviceIds);
}