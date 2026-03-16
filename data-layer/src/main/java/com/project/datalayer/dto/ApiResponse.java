package com.project.datalayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiResponse: Wrapper chuẩn cho tất cả response trả về từ API.
 *
 * Mục đích: Thay vì mỗi endpoint trả về kiểu dữ liệu khác nhau (String, DTO, List...),
 * tất cả đều bọc trong object này để Flutter dễ xử lý đồng nhất.
 *
 * Ví dụ response thành công:
 * { "success": true, "message": "Đăng nhập thành công", "data": { ...UserDTO... } }
 *
 * Ví dụ response lỗi:
 * { "success": false, "message": "Số điện thoại không tồn tại", "data": null }
 *
 * @param <T> Kiểu dữ liệu của trường data (UserDTO, List<RoomDetailDTO>, ...)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // --- Factory methods tiện lợi, không cần gọi builder mỗi lần ---

    /**
     * Tạo response thành công kèm data.
     * Dùng khi: trả về đối tượng hoặc danh sách.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Thành công")
                .data(data)
                .build();
    }

    /**
     * Tạo response thành công kèm data và message tùy chỉnh.
     * Dùng khi: muốn hiển thị thông báo cụ thể (vd: "Tạo hợp đồng thành công").
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Tạo response lỗi.
     * Dùng khi: bắt exception trong GlobalExceptionHandler.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}