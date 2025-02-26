package com.samuel.sniffers.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.samuel.sniffers.api.DateTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

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
        return new ApiResponse<>(HttpStatus.OK.value(), "Success", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), message, data, null);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(HttpStatus.CREATED.value(), message, data, null);
    }

    public static <T> ApiResponse<T> error(int errorCode, String message, T error) {
        return new ApiResponse<>(errorCode, message, null, error);
    }
}