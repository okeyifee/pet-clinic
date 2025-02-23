package com.samuel.sniffers.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.samuel.sniffers.api.DateTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private String message;
    private String dateTime = DateTimeUtils.getCurrentDateTimeReadable();
    private T data;
    private Object error;

    public ApiResponse(int status, String message, T data, Object error) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, null);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data, null);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(200, message, null, null);
    }

    public static <T> ApiResponse<T> error(int errorCode, String message, T error) {
        return new ApiResponse<>(errorCode, message, null, error);
    }
}