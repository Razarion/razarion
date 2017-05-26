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
 * Date: 03.08.2010
 * Time: 22:09:15
 */
public class EventTrackingStart {
    private int clientWidth;
    private int clientHeight;
    private int scrollLeft;
    private int scrollTop;
    private int scrollWidth;
    private int scrollHeight;
    private long clientTimeStamp;
    private String startUuid;

    /**
     * Used by GWT
     */
    public EventTrackingStart() {
    }

    public EventTrackingStart(String startUuid, int clientWidth, int clientHeight, int scrollLeft, int scrollTop, int scrollWidth, int scrollHeight) {
        this.startUuid = startUuid;
        clientTimeStamp = System.currentTimeMillis();
        this.clientWidth = clientWidth;
        this.clientHeight = clientHeight;
        this.scrollLeft = scrollLeft;
        this.scrollTop = scrollTop;
        this.scrollWidth = scrollWidth;
        this.scrollHeight = scrollHeight;
    }

    public EventTrackingStart(String startUuid, int clientWidth, int clientHeight, int scrollLeft, int scrollTop, int scrollWidth, int scrollHeight, long clientTimeStamp) {
        this.startUuid = startUuid;
        this.clientTimeStamp = clientTimeStamp;
        this.clientWidth = clientWidth;
        this.clientHeight = clientHeight;
        this.scrollLeft = scrollLeft;
        this.scrollTop = scrollTop;
        this.scrollWidth = scrollWidth;
        this.scrollHeight = scrollHeight;
    }

    public long getClientTimeStamp() {
        return clientTimeStamp;
    }

    public int getClientWidth() {
        return clientWidth;
    }

    public int getClientHeight() {
        return clientHeight;
    }

    public int getScrollLeft() {
        return scrollLeft;
    }

    public int getScrollTop() {
        return scrollTop;
    }

    public int getScrollWidth() {
        return scrollWidth;
    }

    public int getScrollHeight() {
        return scrollHeight;
    }

    public String getStartUuid() {
        return startUuid;
    }
}
