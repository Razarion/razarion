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

    protected abstract void privateStart(DeferredStartup deferredStartup);

    public void setTaskEnum(StartupTaskEnum taskEnum) {
        this.taskEnum = taskEnum;
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

    public StartupTaskEnum getWaitForBackgroundTask() {
        return taskEnum.getWaitForBackgroundTask();
    }

    @Override
    public String toString() {
        return getClass().getName() + " {" + taskEnum + " duration: " + (duration / 1000.0) + "s}";
    }

}
