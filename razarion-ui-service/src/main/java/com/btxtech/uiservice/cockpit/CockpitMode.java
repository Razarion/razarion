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

import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.SelectionEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 16.11.2010
 * Time: 22:52:52
 */
@ApplicationScoped
public class CockpitMode {
    public enum Mode {
        UNLOAD,
    }
    // private Logger logger = Logger.getLogger(CockpitMode.class.getName());
    private Mode mode;

    public Mode getMode() {
        return mode;
    }

}
