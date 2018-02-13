package com.btxtech.shared.cdimock;

import com.btxtech.shared.system.debugtool.DebugHelper;

import javax.inject.Singleton;

/**
 * Created by Beat
 * on 13.02.2018.
 */
@Singleton
public class TestDebugHelper implements DebugHelper {
    @Override
    public void debugToDb(String debugMessage) {
        System.out.println("TestDebugHelper: " + debugMessage);
    }
}
