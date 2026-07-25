package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    EGYPTIAN_ARABIC("arz", "العامية المصرية", true),
    MODERN_STANDARD_ARABIC("ar", "العربية الفصحى", true),
    ENGLISH("en", "English", false);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EGYPTIAN_ARABIC
        }
    }
}
