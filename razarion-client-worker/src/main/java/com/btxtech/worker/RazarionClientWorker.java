package com.btxtech.worker;

import com.btxtech.shared.rest.LoggingControllerFactory;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.dominokit.rest.DominoRestConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RazarionClientWorker implements EntryPoint {
    private final Logger logger = Logger.getLogger(RazarionClientWorker.class.getName());

    @Override
    public void onModuleLoad() {
        DominoRestConfig.initDefaults().setDefaultResourceRootPath("/rest");
        LoggingControllerFactory.INSTANCE
                .simpleLogger("RazarionClientWorker onModuleLoad")
                .send();

        logger.severe("RazarionClientWorker onModuleLoad");

        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable throwable) {
                logger.log(Level.SEVERE, "UncaughtExceptionHandler", throwable);
            }
        });

        RazarionClientWorkerComponent component = DaggerRazarionClientWorkerComponent.create();
        component.clientGameEngineWorker().init();
    }
}
