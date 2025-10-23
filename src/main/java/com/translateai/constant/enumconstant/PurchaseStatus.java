package com.translateai.constant.enumconstant;

/**
 * Enum for purchase verification status
 */
public enum PurchaseStatus {
    /**
     * Purchase verified successfully
     */
    SUCCESS("SUCCESS"),
    
    /**
     * Product ID not found or doesn't match
     */
    NOT_FOUND("NOT_FOUND"),
    
    /**
     * Invalid or expired token
     */
    INVALID_TOKEN("INVALID_TOKEN"),
    
    /**
     * Server error
     */
    SERVER_ERROR("SERVER_ERROR");

    private final String value;

    PurchaseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

