package com.project.logiclayer.exception;

import com.project.datalayer.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler: Bắt tất cả exception trong ứng dụng và
 * trả về JSON chuẩn thay vì stack trace hoặc trang lỗi HTML mặc định.
 *
 * Tại sao cần class này?
 * Khi một Service ném RuntimeException("Phòng không tồn tại"),
 * nếu không có handler, Spring sẽ trả về HTTP 500 với body là HTML error page.
 * Flutter nhận HTML thay vì JSON → không parse được → app crash.
 *
 * Với GlobalExceptionHandler:
 * HTTP 400/404/500 + body JSON: { "success": false, "message": "Phòng không tồn tại" }
 * Flutter đọc được và hiển thị thông báo đẹp cho người dùng.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Áp dụng cho tất cả @RestController trong ứng dụng.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt ResourceNotFoundException — tài nguyên không tìm thấy.
     * Trả HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Bắt IllegalArgumentException — dữ liệu đầu vào không hợp lệ.
     * Ví dụ: ngày kết thúc trước ngày bắt đầu, số điện thoại đã tồn tại.
     * Trả HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Bắt RuntimeException chung — lỗi nghiệp vụ không thuộc loại trên.
     * Ví dụ: "Phòng đang được thuê", "Hợp đồng chưa hết hạn".
     * Trả HTTP 400.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Bắt mọi exception còn lại — lỗi hệ thống không lường trước.
     * Trả HTTP 500, che giấu chi tiết kỹ thuật với client.
     * Trong production nên log lỗi này ra file/monitoring system.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // TODO: log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi hệ thống. Vui lòng thử lại sau."));
    }
}