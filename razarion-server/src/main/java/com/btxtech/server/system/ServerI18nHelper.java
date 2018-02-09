package com.btxtech.server.system;

import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Beat
 * on 31.01.2018.
 */
@Singleton
public class ServerI18nHelper {
    @Inject
    private ExceptionHandler exceptionHandler;

    public String getString(String key, Locale locale) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("/Razarion", locale);
        try {
            return new String(resourceBundle.getString(key).getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            exceptionHandler.handleException(e);
        }
        return resourceBundle.getString(key);
    }
}
