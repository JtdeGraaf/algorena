package com.algorena.users.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Supported languages using ISO 639-1 two-character codes.
 */
@AllArgsConstructor
@Getter
public enum Language {
    EN("en", "English"),
    NL("nl", "Dutch");

    private final String code;
    private final String displayName;

    /**
     * Get Language enum from ISO code string.
     * Returns EN as default if code is null or invalid.
     */
    public static Language fromCode(String code) {
        if (code == null) {
            return EN;
        }
        for (Language lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return EN; // Default to English if not found
    }
}

