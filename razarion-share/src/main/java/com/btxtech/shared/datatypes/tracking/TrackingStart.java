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

import com.btxtech.shared.datatypes.Index;

/**
 * User: beat
 * Date: 03.08.2010
 * Time: 22:09:15
 */
public class TrackingStart extends DetailedTracking {
    private String gameSessionUuid;
    private int planetId;
    private Index browserWindowDimension;

    public String getGameSessionUuid() {
        return gameSessionUuid;
    }

    public TrackingStart setGameSessionUuid(String gameSessionUuid) {
        this.gameSessionUuid = gameSessionUuid;
        return this;
    }

    public int getPlanetId() {
        return planetId;
    }

    public TrackingStart setPlanetId(int planetId) {
        this.planetId = planetId;
        return this;
    }

    public Index getBrowserWindowDimension() {
        return browserWindowDimension;
    }

    public TrackingStart setBrowserWindowDimension(Index browserWindowDimension) {
        this.browserWindowDimension = browserWindowDimension;
        return this;
    }

    @Override
    public String toString() {
        return "TrackingStart{" +
                "gameSessionUuid='" + gameSessionUuid + '\'' +
                ", planetId=" + planetId +
                ", browserWindowDimension=" + browserWindowDimension +
                '}';
    }
}
