package com.project.datalayer.mapper;

import com.project.cruddata.entity.User;
import com.project.cruddata.dto.UserDTO;
import org.springframework.stereotype.Component;

/**
 * UserMapper: Chuyển đổi giữa Entity User và UserDTO.
 */
@Component
public class UserMapper {

    public UserDTO toDTO(User entity) {
        if (entity == null) return null;

        return UserDTO.builder()
                .userId(entity.getId())
                .phoneNumber(entity.getPhoneNumber())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .idCardNumber(entity.getIdCardNumber())
                // Lấy tên role từ quan hệ với bảng Roles
                .roleName(entity.getRole() != null ? entity.getRole().getRoleName() : null)
                .isActive(entity.getIsActive())
                .build();
    }
    // Lưu ý: Không có toEntity cho password vì password cần được mã hóa riêng ở Service
}