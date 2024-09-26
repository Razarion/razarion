package com.btxtech.client.system;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.05.2015.
 */
public class CdiLogHelper {

    // TODO @Produces
    public Logger createLogger(/*InjectionPoint injectionPoint*/) {
        // return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
        return Logger.getLogger("client");
    }
}
