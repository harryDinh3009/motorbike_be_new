package com.translateai.constant.enumconstant;

import lombok.Getter;

/**
 * Enum định nghĩa các ngôn ngữ được hỗ trợ trong hệ thống dịch thuật
 */
@Getter
public enum Language {
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt"),
    ENGLISH("en", "English", "Tiếng Anh"),
    KOREAN("ko", "Korean", "Tiếng Hàn"),
    JAPANESE("ja", "Japanese", "Tiếng Nhật"),
    CHINESE("zh", "Chinese", "Tiếng Trung"),
    FRENCH("fr", "French", "Tiếng Pháp"),
    GERMAN("de", "German", "Tiếng Đức"),
    SPANISH("es", "Spanish", "Tiếng Tây Ban Nha"),
    THAI("th", "Thai", "Tiếng Thái"),
    RUSSIAN("ru", "Russian", "Tiếng Nga"),
    PORTUGUESE("pt", "Portuguese", "Tiếng Bồ Đào Nha"),
    ITALIAN("it", "Italian", "Tiếng Ý"),
    DUTCH("nl", "Dutch", "Tiếng Hà Lan"),
    ARABIC("ar", "Arabic", "Tiếng Ả Rập"),
    HINDI("hi", "Hindi", "Tiếng Hindi");

    private final String code;
    private final String englishName;
    private final String nativeName;

    Language(String code, String englishName, String nativeName) {
        this.code = code;
        this.englishName = englishName;
        this.nativeName = nativeName;
    }

    /**
     * Tìm Language từ code
     * @param code mã ngôn ngữ (vi, en, ko, ...)
     * @return Language enum hoặc null nếu không tìm thấy
     */
    public static Language fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (Language lang : Language.values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return null;
    }

    /**
     * Kiểm tra code có hợp lệ không
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
}

