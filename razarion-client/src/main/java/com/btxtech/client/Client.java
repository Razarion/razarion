package com.btxtech.client;

import com.btxtech.client.clientI18n.ClientI18nConstants;
import com.btxtech.client.system.LifecycleService;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.CommonUrl;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.Bundle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@EntryPoint
@Bundle("clientI18n/ErraiI18nBundle.properties")
public class Client {
    private Logger logger = Logger.getLogger(Client.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private LifecycleService lifecycleService;

    public Client() {
        GWT.setUncaughtExceptionHandler(e -> {
            if (logger != null) {
                logger.log(Level.SEVERE, "UncaughtExceptionHandler", e);
            } else {
                exceptionHandler.handleException("UncaughtExceptionHandler", e);
            }
        });
        RestClient.setApplicationRoot(CommonUrl.APPLICATION_PATH); // If the html-page is in the faces servlet filter path, the charset is overridden. -> problems with special characters
    }

    @PostConstruct
    public void postConstruct() {
        exceptionHandler.registerWindowCloseHandler();
        try {
            I18nHelper.setLanguage(I18nString.convert(LocaleInfo.getCurrentLocale().getLocaleName()));
            I18nHelper.setConstants(GWT.create(ClientI18nConstants.class));
            TranslationService.setCurrentLocale(LocaleInfo.getCurrentLocale().getLocaleName());
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @AfterInitialization
    public void afterInitialization() {
        lifecycleService.startCold();
    }

}
