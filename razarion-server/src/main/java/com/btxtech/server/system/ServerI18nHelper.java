package com.btxtech.server.system;

import javax.inject.Singleton;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * on 31.01.2018.
 */
@Singleton
public class ServerI18nHelper {

    public String getString(String key, Locale locale) {
        return ResourceBundle.getBundle("Razarion", locale).getString(key);
    }
}
