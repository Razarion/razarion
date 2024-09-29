package com.btxtech.client;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.system.LifecycleService;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.dominokit.rest.DominoRestConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RazarionClient implements EntryPoint {
    private final Logger logger = Logger.getLogger(RazarionClient.class.getName());

    @Override
    public void onModuleLoad() {
        DominoRestConfig.initDefaults().setDefaultResourceRootPath("/rest");

        GWT.setUncaughtExceptionHandler(throwable -> logger.log(Level.SEVERE, "UncaughtExceptionHandler", throwable));

        RazarionClientComponent component = DaggerRazarionClientComponent.create();
        GwtAngularService gwtAngularService = component.gwtAngularService();
        LifecycleService lifecycleService = component.lifecycleService();

        gwtAngularService.init();
        lifecycleService.startCold();
    }
}
