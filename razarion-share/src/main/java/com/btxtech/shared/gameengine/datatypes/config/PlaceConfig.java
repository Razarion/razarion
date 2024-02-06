package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InsideCheckResult;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

import java.util.Collection;

/**
 * Created by Beat
 * 23.07.2016.
 */
@JsType
public class PlaceConfig {
    private Polygon2D polygon2D;
    private DecimalPosition position;
    private Double radius;

    public @Nullable Polygon2D getPolygon2D() {
        return polygon2D;
    }

    public void setPolygon2D(@Nullable Polygon2D polygon2D) {
        this.polygon2D = polygon2D;
    }

    public @Nullable DecimalPosition getPosition() {
        return position;
    }

    public void setPosition(@Nullable DecimalPosition position) {
        this.position = position;
    }

    public @Nullable Double getRadius() {
        return radius;
    }

    public void setRadius(@Nullable Double radius) {
        this.radius = radius;
    }

    @SuppressWarnings("unused") // Used ba angular
    public double toRadiusAngular() {
        if (radius != null) {
            return radius;
        } else {
            return 0;
        }
    }

    public PlaceConfig polygon2D(Polygon2D polygon2D) {
        setPolygon2D(polygon2D);
        return this;
    }

    public PlaceConfig position(DecimalPosition position) {
        setPosition(position);
        return this;
    }

    public PlaceConfig radius(Double radius) {
        setRadius(radius);
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
    @JsIgnore
    @SuppressWarnings("unused") // Used ba angular
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
    @JsIgnore
    @SuppressWarnings("unused") // Used ba angular
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

    @JsIgnore
    @SuppressWarnings("unused") // Used ba angular
    public boolean checkInside(DecimalPosition position) {
        if (this.position != null) {
            if (radius != null) {
                return position.getDistance(this.position) < radius;
            } else {
                return this.position.equalsDelta(position);
            }
        } else if (polygon2D != null) {
            return polygon2D.isInside(position);
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }

    /**
     * Returns false if one of the given positions is not inside this PlaceConfig
     *
     * @param positions given positions
     * @return true if all positions are inside this PlaceConfig
     */
    @JsIgnore
    @SuppressWarnings("unused") // Used ba angular
    public boolean checkInside(Collection<DecimalPosition> positions) {
        return positions.stream().allMatch(this::checkInside);
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
            return new PlaceConfig().position(realm.getPosition().add(absoluteCenter)).radius(realm.getRadius());
        } else if (realm.getPolygon2D() != null) {
            return new PlaceConfig().polygon2D(realm.getPolygon2D().translate(absoluteCenter));
        } else {
            throw new IllegalStateException("Invalid PlaceConfig");
        }
    }
}
