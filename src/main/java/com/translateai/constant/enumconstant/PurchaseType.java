package com.translateai.constant.enumconstant;

/**
 * Enum for purchase types in Google Play
 */
public enum PurchaseType {
    /**
     * One-time in-app purchase
     */
    IN_APP("IN_APP"),
    
    /**
     * Subscription purchase
     */
    SUBSCRIPTION("SUBSCRIPTION");

    private final String value;

    PurchaseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Check if a string value is a valid purchase type
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (PurchaseType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get PurchaseType from string value
     */
    public static PurchaseType fromValue(String value) {
        for (PurchaseType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid purchase type: " + value);
    }
}

