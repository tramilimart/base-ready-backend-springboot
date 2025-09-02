package com.lls.rbac.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {

    // Builder pattern entry point
    public static ApiResponseBuilder body() {
        return new ApiResponseBuilder();
    }

    // Builder class
    public static class ApiResponseBuilder {
        private boolean success = true;
        private String responseCode;
        private String message;
        private Object data;
        private HttpStatus status = HttpStatus.OK;

        public ApiResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public ApiResponseBuilder responseCode(String responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        public ApiResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ApiResponseBuilder data(Object data) {
            this.data = data;
            return this;
        }

        public ApiResponseBuilder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public ResponseEntity<?> build() {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", success);
            responseBody.put("responseCode", responseCode == null || responseCode.isEmpty() ? "200" : responseCode);
            responseBody.put("message", message);
            responseBody.put("data", data);
            responseBody.put("timestamp", System.currentTimeMillis());
            return new ResponseEntity<>(responseBody, status);
        }
    }
}
