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

package com.btxtech.uiservice.cockpit;

import com.btxtech.uiservice.SelectionEvent;

import javax.inject.Singleton;

/**
 * User: beat
 * Date: 16.11.2010
 * Time: 22:52:52
 */
@Singleton
public class CockpitMode {
    public enum Mode {
        UNLOAD
    }

    private Mode mode;

    public void setCockpitMode(Mode mode) {
        this.mode = mode;
    }

    public void clear() {
        mode = null;
    }

    public Mode getMode() {
        return mode;
    }

    public void onSelectionChanged(SelectionEvent selectionEvent) {
        clear();
    }

}
