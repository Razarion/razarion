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

package com.btxtech.client.system.boot;

import com.btxtech.client.system.boot.task.AbstractStartupTask;
import com.btxtech.client.system.boot.task.DeferredStartup;

/**
 * User: beat
 * Date: 08.12.2010
 * Time: 12:55:22
 */
public class ClientRunnerDeferredStartupImpl implements DeferredStartup {
    private boolean isDeferred;
    private boolean isBackground;
    private AbstractStartupTask task;
    private ClientRunner clientRunner;
    private boolean isFinished = false;

    public ClientRunnerDeferredStartupImpl(AbstractStartupTask task, ClientRunner clientRunner) {
        this.task = task;
        this.clientRunner = clientRunner;
    }

    @Override
    public void setDeferred() {
        isDeferred = true;
    }

    @Override
    public void finished() {
        isFinished = true;
        task.correctDeferredDuration();
        clientRunner.onTaskFinished(task, this);
    }

    @Override
    public void failed(Throwable t) {
        isFinished = true;
        task.correctDeferredDuration();
        clientRunner.onTaskFailed(task, t);
    }

    @Override
    public void failed(String error) {
        isFinished = true;
        task.correctDeferredDuration();
        clientRunner.onTaskFailed(task, error, null);
    }

    @Override
    public void setBackground() {
        isBackground = true;
    }

    public boolean isDeferred() {
        return isDeferred;
    }

    @Override
    public boolean isBackground() {
        return isBackground;
    }

    public boolean isFinished() {
        return isFinished;
    }
}
