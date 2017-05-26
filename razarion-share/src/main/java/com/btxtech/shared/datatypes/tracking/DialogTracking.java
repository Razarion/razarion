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
 * Date: 15.11.2011
 * Time: 12:30:41
 */
public class DialogTracking {
    private String startUuid;
    private Integer left;
    private Integer top;
    private Integer width;
    private Integer height;
    private Integer zIndex;
    private String description;
    private boolean appearing;
    private int identityHashCode;
    private long clientTimeStamp;

    /**
     * Used by GWT
     */
    protected DialogTracking() {
    }

    public DialogTracking(String startUuid, Integer left, Integer top, Integer width, Integer height, Integer zIndex, String description, boolean appearing, int identityHashCode, long clientTimeStamp) {
        this.startUuid = startUuid;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.clientTimeStamp = clientTimeStamp;
        this.zIndex = zIndex;
        this.description = description;
        this.appearing = appearing;
        this.identityHashCode = identityHashCode;
    }

    public DialogTracking(String startUuid, int left, int top, int width, int height, Integer zIndex, String description, int identityHashCode) {
        this.startUuid = startUuid;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.zIndex = zIndex;
        this.description = description;
        this.identityHashCode = identityHashCode;
        appearing = true;
        clientTimeStamp = System.currentTimeMillis();
    }

    public DialogTracking(String startUuid, int identityHashCode) {
        this.startUuid = startUuid;
        this.identityHashCode = identityHashCode;
        appearing = false;
        clientTimeStamp = System.currentTimeMillis();
    }

    public Integer getLeft() {
        return left;
    }

    public Integer getTop() {
        return top;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public long getClientTimeStamp() {
        return clientTimeStamp;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAppearing() {
        return appearing;
    }

    public int getIdentityHashCode() {
        return identityHashCode;
    }

    public Integer getZIndex() {
        return zIndex;
    }

    public String getStartUuid() {
        return startUuid;
    }
}
