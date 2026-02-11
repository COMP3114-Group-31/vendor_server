package com.mpu.vendor.exception;

import java.util.Collections;
import java.util.Map;

public class BadRequestException extends RuntimeException {

    private final Map<String, String> details;

    public BadRequestException(String message) {
        super(message);
        this.details = Collections.emptyMap();
    }

    public BadRequestException(String message, String field, String detail) {
        super(message);
        this.details = Collections.singletonMap(field, detail);
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
