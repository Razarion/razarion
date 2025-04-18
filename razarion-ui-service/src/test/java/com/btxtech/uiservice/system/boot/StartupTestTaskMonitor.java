package com.btxtech.uiservice.system.boot;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2017.
 */
@Singleton
public class StartupTestTaskMonitor {
    private List<DeferredStartupTestTask> deferredStartupTestTasks = new ArrayList<>();
    private List<DeferredBackgroundStartupTestTask> deferredBackgroundStartupTestTasks = new ArrayList<>();

    public void addDeferredStartupTestTask(DeferredStartupTestTask deferredStartupTestTask) {
        deferredStartupTestTasks.add(deferredStartupTestTask);
    }

    public DeferredStartupTestTask getDeferredStartupTestTask(int index) {
        return deferredStartupTestTasks.get(index);
    }

    public void addStartupTestTaskMonitor(DeferredBackgroundStartupTestTask deferredBackgroundStartupTestTask) {
        deferredBackgroundStartupTestTasks.add(deferredBackgroundStartupTestTask);
    }

    public DeferredBackgroundStartupTestTask getDeferredBackgroundStartupTestTask(int index) {
        return deferredBackgroundStartupTestTasks.get(index);
    }

}
