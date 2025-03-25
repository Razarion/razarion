package com.btxtech.shared.system.debugtool;

import java.util.logging.Logger;

/**
 * Created by Beat
 * on 13.11.2018.
 */
public class DebugTimeProbe {
    private long timeStamp;
    private final Logger logger = Logger.getLogger(DebugTimeProbe.class.getName());
    private final String name;

    public DebugTimeProbe(String name) {
        this.name = name;
        timeStamp = System.currentTimeMillis();
    }

    public void doProbe(String description) {
        logger.severe(name + ":->" + description + ": " + (System.currentTimeMillis() - timeStamp) + "ms");
        timeStamp = System.currentTimeMillis();
    }
}
