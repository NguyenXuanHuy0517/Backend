package com.project.logiclayer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException: Exception tùy chỉnh cho trường hợp
 * không tìm thấy entity trong DB (thay thế RuntimeException("... not found")).
 *
 * Lợi ích so với RuntimeException thông thường:
 * - GlobalExceptionHandler có thể bắt riêng và trả HTTP 404 (đúng ngữ nghĩa REST)
 * - Các Service ném exception này một cách tường minh, dễ đọc code hơn
 *
 * Cách dùng trong Service:
 *   throw new ResourceNotFoundException("Phòng", "ID", roomId);
 *   → message: "Phòng không tìm thấy với ID: 5"
 *
 * @ResponseStatus: nếu không có GlobalExceptionHandler, Spring MVC tự trả 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resourceName Tên loại tài nguyên: "Phòng", "Hợp đồng", "Người thuê"
     * @param fieldName    Tên trường dùng để tìm: "ID", "Mã hợp đồng", "Số điện thoại"
     * @param fieldValue   Giá trị đã tìm nhưng không thấy
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s không tìm thấy với %s: %s", resourceName, fieldName, fieldValue));
    }

    // Overload đơn giản hơn cho thông báo tùy chỉnh
    public ResourceNotFoundException(String message) {
        super(message);
    }
}