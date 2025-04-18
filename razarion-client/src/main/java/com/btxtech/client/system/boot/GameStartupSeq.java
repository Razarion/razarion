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

import com.btxtech.uiservice.system.boot.StartupSeq;
import com.btxtech.uiservice.system.boot.StartupTaskEnum;

/**
 * User: beat
 * Date: 06.12.2010
 * Time: 22:03:20
 */
public enum GameStartupSeq implements StartupSeq {
    COLD {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return ColdGameStartupTaskEnum.values();
        }
    },
    WARM {
        @Override
        public StartupTaskEnum[] getAbstractStartupTaskEnum() {
            return WarmGameStartupTaskEnum.values();
        }
    }
}
