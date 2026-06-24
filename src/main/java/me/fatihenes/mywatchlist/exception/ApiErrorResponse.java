package me.fatihenes.mywatchlist.exception;

import java.time.LocalDateTime;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message,
        Map<String, String> fieldErrors) {

    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, null);
    }

    public static ApiErrorResponse ofValidation(Map<String, String> fieldErrors) {
        return new ApiErrorResponse(LocalDateTime.now(), 400, "Validation Error", null,
                fieldErrors);
    }
}
