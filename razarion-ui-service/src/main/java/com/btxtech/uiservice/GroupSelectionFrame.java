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

import java.util.ArrayList;
import java.util.List;

/**
 * User: beat
 * Date: 08.11.2009
 * Time: 11:55:18
 */
public class GroupSelectionFrame {
    private static final int MIN_PIXEL_FRAME_SIZE = 10;
    private DecimalPosition start;
    private Rectangle2D rectangle;

    public GroupSelectionFrame(DecimalPosition start) {
        this.start = start;
    }

    public void onMove(DecimalPosition position) {
        generateSelectionRectangle(position);
    }

    private void generateSelectionRectangle(DecimalPosition position) {
        double x = Math.min(start.getX(), position.getX());
        double y = Math.min(start.getY(), position.getY());
        double width = Math.abs(start.getX() - position.getX());
        double height = Math.abs(start.getY() - position.getY());
        rectangle = new Rectangle2D(x, y, width, height);
    }

    public Rectangle2D finished(DecimalPosition position) {
        generateSelectionRectangle(position);
        if (rectangle == null) {
            return null;
        }
        if (!rectangle.hasMinSize(MIN_PIXEL_FRAME_SIZE)) {
            return null;
        }
        return rectangle;
    }

    public List<Vertex> generateVertices() {
        double z = 0;
        List<Vertex> vertices = new ArrayList<>();
        // Line 1
        vertices.add(new Vertex(rectangle.getStart(), z));
        vertices.add(new Vertex(rectangle.endX(), rectangle.startY(), z));
        // Line 2
        vertices.add(new Vertex(rectangle.endX(), rectangle.startY(), z));
        vertices.add(new Vertex(rectangle.getEnd(), z));
        // Line 3
        vertices.add(new Vertex(rectangle.getEnd(), z));
        vertices.add(new Vertex(rectangle.startX(), rectangle.endY(), z));
        // Line 4
        vertices.add(new Vertex(rectangle.startX(), rectangle.endY(), z));
        vertices.add(new Vertex(rectangle.getStart(), z));
        return vertices;
    }

    public Rectangle2D getRectangle() {
        return rectangle;
    }
}
