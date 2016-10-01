package com.btxtech.shared.datatypes;


import java.util.Map;

/**
 * User: beat
 * Date: 13.01.13
 * Time: 13:14
 */
public class I18nString {
    public static final String DEFAULT = "DEFAULT"; // Errai Jackson JSON marshaller can not handle enum as Map keys
    public static final String DE = "DE"; // Errai Jackson JSON marshaller can not handle enum as Map keys

    private Map<String, String> localizedStrings;

    public static String convert(String localName) {
        if (localName.toLowerCase().startsWith("default")) {
            return DEFAULT;
        } else if (localName.toLowerCase().startsWith("de")) {
            return DE;
        } else {
            return DEFAULT;
        }
    }

    /**
     * Used by GWT
     */
    public I18nString() {
    }

    public I18nString(Map<String, String> localizedStrings) {
        this.localizedStrings = localizedStrings;
    }

    public String getString(String language) {
        if (localizedStrings == null) {
            return null;
        }
        String value = localizedStrings.get(language);
        if (value != null) {
            return value;
        } else {
            return localizedStrings.get(DEFAULT);
        }
    }

    // Only used for JAX-RS JSON
    public Map<String, String> getLocalizedStrings() {
        return localizedStrings;
    }

    // Only used for JAX-RS JSON
    public void setLocalizedStrings(Map<String, String> localizedStrings) {
        this.localizedStrings = localizedStrings;
    }
}
