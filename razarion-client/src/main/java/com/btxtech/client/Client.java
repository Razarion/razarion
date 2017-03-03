package com.btxtech.client;

import com.btxtech.client.clientI18n.ClientI18nConstants;
import com.btxtech.client.system.boot.GameStartupSeq;
import com.btxtech.client.system.boot.StartupProgressListenerImpl;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.system.boot.ClientRunner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.Bundle;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
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
    private ClientRunner clientRunner;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<StartupProgressListenerImpl> startupListenerInstance;

    public Client() {
        GWT.setUncaughtExceptionHandler(e -> {
            if (logger != null) {
                logger.log(Level.SEVERE, "UncaughtExceptionHandler", e);
            } else {
                GWT.log("UncaughtExceptionHandler", e);
            }
        });
        RestClient.setApplicationRoot(RestUrl.APPLICATION_PATH);
    }

    @PostConstruct
    public void postConstruct() {
        try {
            I18nHelper.setLanguage(I18nString.convert(LocaleInfo.getCurrentLocale().getLocaleName()));
            I18nHelper.setConstants(GWT.create(ClientI18nConstants.class));
            TranslationService.setCurrentLocale(LocaleInfo.getCurrentLocale().getLocaleName());
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }

        clientRunner.addStartupProgressListener(startupListenerInstance.get());
    }

    @AfterInitialization
    public void afterInitialization() {
        try {
            clientRunner.start(GameStartupSeq.COLD_SIMULATED);
            // clientRunner.start(GameStartupSeq.COLD_EXPERIMENTAL);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Start failed", throwable);
        }
    }

}
