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

import com.btxtech.shared.system.alarm.Alarm;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 13:01:44
 */
public class DeferredStartup {
    private boolean isDeferred;
    private boolean isBackground;
    private AbstractStartupTask task;
    private Boot boot;
    private boolean isFinished = false;

    public DeferredStartup(AbstractStartupTask task, Boot boot) {
        this.task = task;
        this.boot = boot;
    }

    public void setDeferred() {
        isDeferred = true;
    }

    public void finished() {
        isFinished = true;
        task.correctDeferredDuration();
        boot.onTaskFinished(task, this);
    }

    public void failed(Throwable t) {
        isFinished = true;
        task.correctDeferredDuration();
        boot.onTaskFailed(task, t);
    }

    public void failed(String error) {
        isFinished = true;
        task.correctDeferredDuration();
        boot.onTaskFailed(task, error, null);
    }

    public void fallback(Alarm.Type alarmType) {
        boot.onFallback(alarmType);
    }

    public void setBackground() {
        isBackground = true;
    }

    public boolean isDeferred() {
        return isDeferred;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public StartupTaskEnum getStartupTaskEnum() {
        return task.getTaskEnum();
    }
}
