package com.juansierra.global_invoice_api.integration;

public class LegacyServiceException extends RuntimeException {

    public LegacyServiceException(String message) {
        super(message);
    }

    public LegacyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
