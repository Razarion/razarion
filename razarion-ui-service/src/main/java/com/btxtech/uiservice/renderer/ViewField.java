package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 03.06.2016.
 */
public class ViewField {
    private double z;
    private DecimalPosition bottomLeft;
    private DecimalPosition bottomRight;
    private DecimalPosition topRight;
    private DecimalPosition topLeft;

    public ViewField(double z) {
        this.z = z;
    }

    public void setBottomLeft(DecimalPosition bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public void setBottomRight(DecimalPosition bottomRight) {
        this.bottomRight = bottomRight;
    }

    public void setTopRight(DecimalPosition topRight) {
        this.topRight = topRight;
    }

    public void setTopLeft(DecimalPosition topLeft) {
        this.topLeft = topLeft;
    }

    public Vertex getBottomLeftVertex() {
        return new Vertex(bottomLeft, z);
    }

    public Vertex getBottomRightVertex() {
        return new Vertex(bottomRight, z);
    }

    public Vertex getTopRightVertex() {
        return new Vertex(topRight, z);
    }

    public Vertex getTopLeftVertex() {
        return new Vertex(topLeft, z);
    }

    public Vertex calculateLongestLegZ(Vertex origin) {
        if (hasNullPosition()) {
            return null;
        }
        Vertex bottomLeftWithZ = new Vertex(bottomLeft, z);
        Vertex bottomRightWithZ = new Vertex(bottomRight, z);
        Vertex topRightWithZ = new Vertex(topRight, z);
        Vertex topLeftWithZ = new Vertex(topLeft, z);

        double maxDistance = bottomLeftWithZ.distance(origin);
        Vertex farthest = bottomLeftWithZ;

        double distance = bottomRightWithZ.distance(origin);
        if (maxDistance < distance) {
            maxDistance = distance;
            farthest = bottomRightWithZ;
        }

        distance = topRightWithZ.distance(origin);
        if (maxDistance < distance) {
            maxDistance = distance;
            farthest = topRightWithZ;
        }

        distance = topLeftWithZ.distance(origin);
        if (maxDistance > distance) {
            farthest = topLeftWithZ;
        }

        return farthest;
    }

    public Vertex calculateShortestLegZ(Vertex origin) {
        if (hasOnlyNullPosition()) {
            return null;
        }

        double min = Double.MAX_VALUE;
        Vertex zNearPosition = null;
        for (DecimalPosition position : toList()) {
            if (position == null) {
                continue;
            }
            Vertex viewFieldCorner = new Vertex(position, z);
            double distance = viewFieldCorner.distance(origin);
            if (distance < min) {
                min = distance;
                zNearPosition = viewFieldCorner;
            }
        }
        if (zNearPosition != null) {
            return zNearPosition;
        } else {
            return null;
        }
    }

    public boolean hasNullPosition() {
        return bottomLeft == null || bottomRight == null || topRight == null || topLeft == null;
    }

    public boolean hasOnlyNullPosition() {
        return bottomLeft == null && bottomRight == null && topRight == null && topLeft == null;
    }

    /**
     * corner index 0: view field bottom left projected on world ground or null if not projected on ground
     * corner index 1: view field bottom right projected on world ground or null if not projected on ground
     * corner index 2: view field top right projected on world ground or null if not projected on ground
     * corner index 3: view field top left projected on world ground or null if not projected on ground
     *
     * @return position as list
     */
    public List<DecimalPosition> toList() {
        return Arrays.asList(bottomLeft, bottomRight, topRight, topLeft);
    }

    public Polygon2D toPolygon() {
        return new Polygon2D(toList());
    }

    /**
     * Axis-aligned minimum bounding box
     * <p>
     * https://en.wikipedia.org/wiki/Minimum_bounding_box#Axis-aligned_minimum_bounding_box
     *
     * @return Axis-aligned minimum bounding box
     */
    public ViewField calculateAabb() {
        Rectangle2D rect = calculateAabbRectangle();
        ViewField viewField = new ViewField(z);
        viewField.setBottomLeft(rect.getStart());
        viewField.setBottomRight(new DecimalPosition(rect.endX(), rect.startY()));
        viewField.setTopRight(rect.getEnd());
        viewField.setTopLeft(new DecimalPosition(rect.startX(), rect.endY()));
        return viewField;
    }

    public Rectangle2D calculateAabbRectangle() {
        DecimalPosition newBottomLeft = GeometricUtil.calculateMinimalPosition(bottomLeft, bottomRight, topRight, topLeft);
        DecimalPosition newTopRight = GeometricUtil.calculateMaximalPosition(bottomLeft, bottomRight, topRight, topLeft);
        return new Rectangle2D(newBottomLeft, newTopRight);
    }

    public boolean isInside(Rectangle2D rectangle2D) {
        Polygon2D viewPolygon = new Polygon2D(toList());
        return viewPolygon.isInside(rectangle2D.toCorners());
    }

    public boolean isInside(DecimalPosition decimalPosition) {
        Polygon2D viewPolygon = new Polygon2D(toList());
        return viewPolygon.isInside(decimalPosition);
    }

    public DecimalPosition calculateCenter() {
        if (hasNullPosition()) {
            throw new IllegalStateException("Can not calculate center if a position is null");
        }
        return bottomLeft.add(bottomRight).add(topRight).add(topLeft).divide(4);
    }

    public double getZ() {
        return z;
    }

    public DecimalPosition getBottomLeft() {
        return bottomLeft;
    }

    public DecimalPosition getBottomRight() {
        return bottomRight;
    }

    public DecimalPosition getTopRight() {
        return topRight;
    }

    public DecimalPosition getTopLeft() {
        return topLeft;
    }
}
