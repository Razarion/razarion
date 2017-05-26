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

package com.btxtech.shared.datatypes.tracking;

/**
 * User: beat
 * Date: 24.12.2010
 * Time: 12:30:41
 */
public class TerrainScrollTracking {
    private int left;
    private int top;
    private long clientTimeStamp;
    private String startUuid;

    /**
     * Used by GWT
     */
    public TerrainScrollTracking() {
    }

    public TerrainScrollTracking(String startUuid, int left, int top) {
        this.startUuid = startUuid;
        this.left = left;
        this.top = top;
        clientTimeStamp = System.currentTimeMillis();
    }

    public TerrainScrollTracking(String startUuid, int left, int top, long clientTimeStamp) {
        this.startUuid = startUuid;
        this.left = left;
        this.top = top;
        this.clientTimeStamp = clientTimeStamp;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public long getClientTimeStamp() {
        return clientTimeStamp;
    }

    public String getStartUuid() {
        return startUuid;
    }
}
