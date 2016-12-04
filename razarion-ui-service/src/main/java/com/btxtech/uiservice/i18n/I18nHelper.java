package com.btxtech.uiservice.i18n;

import com.btxtech.shared.datatypes.I18nString;

/**
 * Created with IntelliJ IDEA.
 * User: beat
 * Date: 03.01.13
 * Time: 14:53
 */
public class I18nHelper {
    private static I18nConstants constants;
    private static String language = "default";

    public static String getLocalizedString(I18nString i18nString) {
        return i18nString.getString(language);
    }

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String language) {
        I18nHelper.language = language;
    }

    public static I18nConstants getConstants() {
        return constants;
    }

    public static void setConstants(I18nConstants constants) {
        I18nHelper.constants = constants;
    }
}
