package com.btxtech.client.clientI18n;

import com.btxtech.shared.datatypes.I18nString;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;

/**
 * Created with IntelliJ IDEA.
 * User: beat
 * Date: 03.01.13
 * Time: 14:53
 */
public class ClientI18nHelper {
    public static final ClientI18nConstants CONSTANTS = GWT.create(ClientI18nConstants.class);
    public static final String LANGUAGE;

    static {
        LANGUAGE = I18nString.convert(LocaleInfo.getCurrentLocale().getLocaleName());
    }

    public static String getLocalizedString(I18nString i18nString) {
        return i18nString.getString(LANGUAGE);
    }
}
