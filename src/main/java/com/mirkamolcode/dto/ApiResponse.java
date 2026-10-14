package com.mirkamolcode.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private int statusCode;
    private T body;

    public ApiResponse() {
        this.statusCode = 200;
    }

    public ApiResponse(boolean success, String message) {
        this(success, message, 200, null);
    }

    public ApiResponse(boolean success, String message, int statusCode) {
        this(success, message, statusCode, null);
    }

    public ApiResponse(boolean success, String message, T body) {
        this(success, message, 200, body);
    }

    public ApiResponse(boolean success, String message, int statusCode, T body) {
        this.success = success;
        this.message = message;
        this.statusCode = statusCode;
        this.body = body;
    }

    public static <T> ApiResponse<T> success(String message, T body) {
        return new ApiResponse<>(true, message, 200, body);
    }

    public static <T> ApiResponse<T> success(String message, int statusCode, T body) {
        return new ApiResponse<>(true, message, statusCode, body);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, 200, null);
    }

    public static <T> ApiResponse<T> success(String message, int statusCode) {
        return new ApiResponse<>(true, message, statusCode, null);
    }

    public static <T> ApiResponse<T> success(T body) {
        return new ApiResponse<>(true, "Operation successful", 200, body);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
