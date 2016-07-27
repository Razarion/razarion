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

package com.btxtech.shared.gameengine.datatypes;

import java.util.Collection;
import java.util.Iterator;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 15:09:53
 */
public enum SurfaceType {
    NONE,
    WATER,
    LAND,
    COAST;

    public static String toString(Collection<SurfaceType> surfaceTypes) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("[");
        for (Iterator<SurfaceType> iterator = surfaceTypes.iterator(); iterator.hasNext(); ) {
            SurfaceType surfaceType = iterator.next();
            stringBuffer.append(surfaceType.name());
            if (iterator.hasNext()) {
                stringBuffer.append(", ");
            }
        }
        stringBuffer.append("]");
        return stringBuffer.toString();
    }
}
