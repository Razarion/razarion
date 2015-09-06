package com.btxtech.client;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.ioc.client.api.EntryPoint;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@EntryPoint
public class Webgl {
    // @Inject
    private Logger logger = Logger.getLogger(Webgl.class.getName());

    public Webgl() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                if (logger != null) {
                    logger.log(Level.SEVERE, "UncaughtExceptionHandler", e);
                } else {
                    GWT.log("UncaughtExceptionHandler", e);
                }
            }
        });
    }
}
