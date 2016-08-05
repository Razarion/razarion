package com.btxtech.shared.datatypes;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.Map;

/**
 * User: beat
 * Date: 13.01.13
 * Time: 13:14
 */
@Portable
public class I18nString {
    public enum Language {
        DEFAULT,
        DE
    }

    private Map<Language, String> localizedStrings;

    public static Language convert(String localName) {
        if (localName.toLowerCase().startsWith("default")) {
            return Language.DEFAULT;
        } else if (localName.toLowerCase().startsWith("de")) {
            return Language.DE;
        } else {
            return Language.DEFAULT;
        }
    }

    /**
     * Used by GWT
     */
    public I18nString() {
    }

    public I18nString(Map<Language, String> localizedStrings) {
        this.localizedStrings = localizedStrings;
    }

    public String getString() {
        return getString(Language.DEFAULT);
    }

    public String getString(Language language) {
        if (localizedStrings == null) {
            return null;
        }
        String value = localizedStrings.get(language);
        if (value != null) {
            return value;
        } else {
            return localizedStrings.get(Language.DEFAULT);
        }
    }
}
