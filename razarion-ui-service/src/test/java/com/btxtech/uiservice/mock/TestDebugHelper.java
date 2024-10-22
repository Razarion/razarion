package com.btxtech.uiservice.mock;

import com.btxtech.shared.system.debugtool.DebugHelper;

/**
 * Created by Beat
 * on 29.11.2018.
 */
public class TestDebugHelper implements DebugHelper {
    @Override
    public void debugToDb(String debugMessage) {
        System.out.println("TestDebugHelper.debugToDb(): " + debugMessage);
    }

    @Override
    public void debugToConsole(String debugMessage) {
        System.out.println("TestDebugHelper.debugToConsole(): " + debugMessage);
    }
}
