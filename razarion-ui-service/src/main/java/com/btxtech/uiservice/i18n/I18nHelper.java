package com.btxtech.uiservice.i18n;

import com.btxtech.shared.datatypes.I18nString;
/**
 * Created with IntelliJ IDEA.
 * User: beat
 * Date: 03.01.13
 * Time: 14:53
 */
public class I18nHelper {

    public static String getLocalizedString(I18nString i18nString) {
        if(i18nString == null) {
            return "???";
        }
        return i18nString.getString();
    }
}
