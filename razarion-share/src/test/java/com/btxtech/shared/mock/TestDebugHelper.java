package com.btxtech.shared.mock;

import com.btxtech.shared.system.debugtool.DebugHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 13.02.2018.
 */
@Singleton
public class TestDebugHelper implements DebugHelper {

    @Inject
    public TestDebugHelper() {
    }

    @Override
    public void debugToDb(String debugMessage) {
        System.out.println("TestDebugHelper.debugToDb(): " + debugMessage);
    }

    @Override
    public void debugToConsole(String debugMessage) {
        System.out.println("TestDebugHelper.debugToConsole(): " + debugMessage);
    }
}
