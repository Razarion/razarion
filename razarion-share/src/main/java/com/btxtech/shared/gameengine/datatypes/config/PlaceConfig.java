package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InsideCheckResult;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

/**
 * Created by Beat
 * 23.07.2016.
 */
public class PlaceConfig {
    private Polygon2D polygon2D;
    private DecimalPosition position;
    private Double radius;

    public Polygon2D getPolygon2D() {
        return polygon2D;
    }

    public PlaceConfig setPolygon2D(Polygon2D polygon2D) {
        this.polygon2D = polygon2D;
        return this;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public PlaceConfig setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public Double getRadius() {
        return radius;
    }

    public PlaceConfig setRadius(Double radius) {
        this.radius = radius;
        return this;
    }

    public Rectangle2D toAabb() {
        if (position != null) {
            if (radius != null) {
                return new Rectangle2D(position.getX() - radius, position.getY() - radius, radius * 2, radius * 2);
            } else {
                return null;
            }
        } else if (polygon2D != null) {
            return polygon2D.toAabb();
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }

    /**
     * If PlaceConfig contains a polygon, only the position is checked.
     * NO CHECK FOR THE RADIUS IS PERFORMED
     *
     * @param syncItem to check
     * @return true if inside
     */
    public boolean checkInside(SyncItem syncItem) {
        if (position != null) {
            if (radius != null) {
                return syncItem.getSyncPhysicalArea().overlap(position, radius);
            } else {
                return syncItem.getSyncPhysicalArea().overlap(position);
            }
        } else if (polygon2D != null) {
            return polygon2D.isInside(syncItem.getSyncPhysicalArea().getPosition2d());
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }

    /**
     * If PlaceConfig contains a polygon, only the position is checked.
     * NO CHECK FOR THE RADIUS IS PERFORMED
     *
     * @param position position to check
     * @param radius   radius to check
     * @return true if inside
     */
    public boolean checkInside(DecimalPosition position, double radius) {
        if (this.position != null) {
            if (this.radius != null) {
                return position.getDistance(this.position) < this.radius + radius;
            } else {
                return position.getDistance(this.position) < radius;
            }
        } else if (polygon2D != null) {
            return polygon2D.isInside(position);
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }

    public boolean checkInside(DecimalPosition position) {
        if (this.position != null) {
            return this.position.equalsDelta(position);
        } else if (polygon2D != null) {
            return polygon2D.isInside(position);
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }

    public boolean checkAdjoins(Rectangle2D rectangle2D) {
        if (position != null) {
            if (radius != null) {
                return rectangle2D.adjoinsCircleExclusive(position, radius);
            } else {
                return rectangle2D.contains(position);
            }
        } else if (polygon2D != null) {
            return polygon2D.checkInside(rectangle2D) != InsideCheckResult.OUTSIDE;
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }

    public static PlaceConfig cloneWithAbsolutePosition(PlaceConfig realm, DecimalPosition absoluteCenter) {
        if (realm == null) {
            return null;
        }
        if (realm.getPosition() != null) {
            return new PlaceConfig().setPosition(realm.getPosition().add(absoluteCenter)).setRadius(realm.getRadius());
        } else if (realm.getPolygon2D() != null) {
            return new PlaceConfig().setPolygon2D(realm.getPolygon2D().translate(absoluteCenter));
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }
}
