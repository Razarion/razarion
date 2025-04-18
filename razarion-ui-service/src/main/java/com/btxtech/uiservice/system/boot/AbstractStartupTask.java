/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.uiservice.system.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 11:29:57
 */
public abstract class AbstractStartupTask {
    private long startTime;
    private long duration;
    private StartupTaskEnum taskEnum;
    private boolean isBackground = false;
    private List<StartupTaskEnum> waitForBackgroundTasks;

    protected abstract void privateStart(DeferredStartup deferredStartup);

    public void setTaskEnum(StartupTaskEnum taskEnum) {
        this.taskEnum = taskEnum;
        if (taskEnum.getWaitForBackgroundTasks() != null) {
            waitForBackgroundTasks = new ArrayList<>(Arrays.asList(taskEnum.getWaitForBackgroundTasks()));
        }
    }

    public void start(DeferredStartup deferredStartup) {
        startTime = System.currentTimeMillis();
        try {
            privateStart(deferredStartup);
            isBackground = deferredStartup.isBackground();
        } finally {
            duration = System.currentTimeMillis() - startTime;
        }
    }

    public long getStartTime() {
        return startTime;
    }

    protected void overrideStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public StartupTaskEnum getTaskEnum() {
        return taskEnum;
    }

    public StartupTaskInfo createStartupTaskInfo() {
        return createStartupTaskInfo(null);
    }

    public StartupTaskInfo createStartupTaskInfo(String error) {
        StartupTaskInfo startupTaskInfo = new StartupTaskInfo(taskEnum, startTime, duration);
        startupTaskInfo.setErrorText(error);
        return startupTaskInfo;
    }

    public void correctDeferredDuration() {
        duration = System.currentTimeMillis() - startTime;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public boolean isWaitingForBackgroundTasks() {
        return waitForBackgroundTasks != null && !waitForBackgroundTasks.isEmpty();
    }

    public void removeFinishedBackgroundTask(StartupTaskEnum taskEnum) {
        if (isWaitingForBackgroundTasks()) {
            waitForBackgroundTasks.remove(taskEnum);
        }
    }

    public void removeFinishedBackgroundTasks(List<DeferredStartup> deferredBackgroundStartups) {
        if (isWaitingForBackgroundTasks()) {
            waitForBackgroundTasks.removeIf(startupTaskEnum -> getBackgroundTask(deferredBackgroundStartups, startupTaskEnum).isFinished());
        }
    }

    private DeferredStartup getBackgroundTask(List<DeferredStartup> deferredBackgroundStartups, StartupTaskEnum waitForBackgroundTaskEnum) {
        for (DeferredStartup deferredBackgroundStartup : deferredBackgroundStartups) {
            if (deferredBackgroundStartup.getStartupTaskEnum() == waitForBackgroundTaskEnum) {
                return deferredBackgroundStartup;
            }
        }
        throw new IllegalStateException("No deferred background task found for: " + waitForBackgroundTaskEnum);
    }

    @Override
    public String toString() {
        return getClass().getName() + " {" + taskEnum + " duration: " + (duration / 1000.0) + "s}";
    }
}
