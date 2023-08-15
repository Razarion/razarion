package com.btxtech.shared.datatypes;


import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import java.util.HashMap;
import java.util.Map;

/**
 * User: beat
 * Date: 13.01.13
 * Time: 13:14
 */
@JsType
public class I18nString {
    public static final String DEFAULT = "DEFAULT"; // Errai Jackson JSON marshaller can not handle enum as Map keys
    public static final String DE = "DE"; // Errai Jackson JSON marshaller can not handle enum as Map keys
    public static final String EN = "EN"; // Errai Jackson JSON marshaller can not handle enum as Map keys

    private Map<String, String> localizedStrings;

    public static String convert(String localName) {
        if (localName.toLowerCase().startsWith("default")) {
            return DEFAULT;
        } else if (localName.toLowerCase().startsWith("de")) {
            return DE;
        } else if (localName.toLowerCase().startsWith("en")) {
            return EN;
        } else {
            return DEFAULT;
        }
    }

    /**
     * Used by GWT
     */
    @JsIgnore
    public I18nString() {
    }

    @JsIgnore
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
            return defaultWithFallback();
        }
    }

    public void setString(String language, String s) {
        if (localizedStrings == null) {
            localizedStrings = new HashMap<>();
        }
        localizedStrings.put(language, s);
    }

    // Only used for JAX-RS JSON
    public Map<String, String> getLocalizedStrings() {
        return localizedStrings;
    }

    // Only used for JAX-RS JSON
    public void setLocalizedStrings(Map<String, String> localizedStrings) {
        this.localizedStrings = localizedStrings;
    }

    private String defaultWithFallback() {
        String s = localizedStrings.get(DEFAULT);
        if (s == null) {
            s = localizedStrings.get(EN);
        }
        return s;
    }
}
