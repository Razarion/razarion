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
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: beat
 * Date: 08.11.2009
 * Time: 11:55:18
 */
public class GroupSelectionFrame {
    private Vertex start;
    private Rectangle2D rectangle2D;
    private List<Vertex> corners;

    public GroupSelectionFrame(Vertex start) {
        this.start = start;
    }

    public void onMove(Vertex position) {
        DecimalPosition delta = start.toXY().sub(position.toXY());
        if (MathHelper.compareWithPrecision(delta.getX(), 0.0) || MathHelper.compareWithPrecision(delta.getY(), 0.0)) {
            rectangle2D = null;
        } else {
            double x = Math.min(start.getX(), position.getX());
            double y = Math.min(start.getY(), position.getY());
            double width = Math.abs(start.getX() - position.getX());
            double height = Math.abs(start.getY() - position.getY());
            rectangle2D = new Rectangle2D(x, y, width, height);
        }
        setupCorner4Renderer(position);
    }

    public Rectangle2D getRectangle2D() {
        return rectangle2D;
    }

    public DecimalPosition getStart2D() {
        return start.toXY();
    }

    public List<Vertex> getCorners() {
        return corners;
    }

    private void setupCorner4Renderer(Vertex end) {
        corners = new ArrayList<>();
        corners.add(new Vertex(start.getX(), start.getY(), end.getZ()));
        corners.add(new Vertex(end.getX(), start.getY(), end.getZ()));
        corners.add(end);
        corners.add(new Vertex(start.getX(), end.getY(), end.getZ()));
    }
}
