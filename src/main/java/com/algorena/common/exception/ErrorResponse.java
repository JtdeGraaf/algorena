package com.algorena.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Standard error response returned by the API for all error scenarios")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2025-11-04T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP status reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Invalid input provided")
    @Nullable
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/products")
    private String path;

    @Schema(description = "List of validation errors (only present for validation failures)")
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ValidationError", description = "Validation error details for a specific field")
    public static class ValidationError {

        @Schema(description = "Name of the field that failed validation", example = "email")
        private String field;

        @Schema(description = "Validation error message", example = "must be a valid email address")
        @Nullable
        private String message;
    }
}
