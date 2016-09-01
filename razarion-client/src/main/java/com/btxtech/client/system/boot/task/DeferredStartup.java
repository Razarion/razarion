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

package com.btxtech.client.system.boot.task;

/**
 * User: beat
 * Date: 04.12.2010
 * Time: 13:01:44
 */
public interface DeferredStartup {
    public static final String NO_CONNECTION = "No connection";
    public static final String NO_SYNC_INFO = "No synchronization information received";

    void setDeferred();

    void finished();

    void failed(Throwable throwable);

    void failed(String error);

    void setBackground();

    boolean isBackground();
}
