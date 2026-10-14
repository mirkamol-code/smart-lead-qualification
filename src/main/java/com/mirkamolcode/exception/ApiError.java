package com.mirkamolcode.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        boolean success,
        String message,
        int statusCode,
        String path,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        ZonedDateTime zonedDateTime,
        List<String> errors
) {
    public static ApiError of(String message, int statusCode, String path) {
        return new ApiError(false, message, statusCode, path, ZonedDateTime.now(), null);
    }

    public static ApiError of(String message, int statusCode, String path, List<String> errors) {
        return new ApiError(false, message, statusCode, path, ZonedDateTime.now(), errors);
    }
}
