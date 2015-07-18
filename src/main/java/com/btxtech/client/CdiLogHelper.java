package com.btxtech.client;

import javax.enterprise.inject.Produces;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 29.05.2015.
 */
public class CdiLogHelper {

    @Produces
    public Logger createLogger() {
        return Logger.getLogger("xxx");
    }
}
