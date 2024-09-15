package com.btxtech.client;

import com.btxtech.client.clientI18n.ClientI18nConstants;
import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.LifecycleService;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Client {
    private final Logger logger = Logger.getLogger(Client.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private LifecycleService lifecycleService;
    @Inject
    private GwtAngularService gwtAngularService;

    public Client() {
        GWT.setUncaughtExceptionHandler(e -> {
            if (logger != null) {
                logger.log(Level.SEVERE, "UncaughtExceptionHandler", e);
            } else {
                exceptionHandler.handleException("UncaughtExceptionHandler", e);
            }
        });
    }

    @PostConstruct
    public void postConstruct() {
        exceptionHandler.registerWindowCloseHandler();
        try {
            I18nHelper.setLanguage(I18nString.convert(LocaleInfo.getCurrentLocale().getLocaleName()));
            I18nHelper.setConstants(GWT.create(ClientI18nConstants.class));
            // TranslationService.setCurrentLocale(LocaleInfo.getCurrentLocale().getLocaleName());
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @SuppressWarnings("unused")
    public void afterInitialization() {
        gwtAngularService.init();
        lifecycleService.startCold();
    }

}
