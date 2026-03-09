package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserDTO: Dùng để trả về thông tin người dùng an toàn.
 * Không chứa password_hash.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long userId;
    private String phoneNumber;
    private String fullName;
    private String email;
    private String idCardNumber;
    private String roleName; // Tên quyền hạn hiển thị
    private Boolean isActive;
}
