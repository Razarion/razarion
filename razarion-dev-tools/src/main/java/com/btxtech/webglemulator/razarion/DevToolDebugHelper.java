package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.system.debugtool.DebugHelper;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * on 13.02.2018.
 */
@ApplicationScoped
public class DevToolDebugHelper implements DebugHelper {
    @Override
    public void debugToDb(String debugMessage) {
        System.out.println("DevToolDebugHelper: " + debugMessage);
    }
}
