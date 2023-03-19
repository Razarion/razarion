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

package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.utils.MathHelper;

/**
 * User: beat
 * Date: 08.11.2009
 * Time: 11:55:18
 */
public class GroupSelectionFrame {
    private static final double MIN_DISTANCE = 0.1;
    private DecimalPosition start;
    private Rectangle2D rectangle2D;

    public GroupSelectionFrame(DecimalPosition start) {
        this.start = start;
    }

    public void onMove(DecimalPosition position) {
        DecimalPosition delta = start.sub(position);
        if (MathHelper.compareWithPrecision(delta.getX(), 0, MIN_DISTANCE) || MathHelper.compareWithPrecision(delta.getY(), 0, MIN_DISTANCE)) {
            rectangle2D = null;
        } else {
            double x = Math.min(start.getX(), position.getX());
            double y = Math.min(start.getY(), position.getY());
            double width = Math.abs(start.getX() - position.getX());
            double height = Math.abs(start.getY() - position.getY());
            rectangle2D = new Rectangle2D(x, y, width, height);
        }
    }

    public Rectangle2D getRectangle2D() {
        return rectangle2D;
    }

    public DecimalPosition getStart2D() {
        return start;
    }
}
