package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 07.05.2017.
 */
@Entity
@Table(name = "PLACE_CONFIG")
public class PlaceConfigEntity extends BaseEntity {
    @ElementCollection
    @CollectionTable(name = "PLACE_CONFIG_POSITION_POLYGON", joinColumns = @JoinColumn(name = "OWNER_ID"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> polygon2D;
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "x", column = @Column), @AttributeOverride(name = "y", column = @Column)})
    private DecimalPosition position;
    private Double radius;

    public static PlaceConfig toPlaceConfig(PlaceConfigEntity entity) {
        if (entity != null) {
            return entity.toPlaceConfig();
        }
        return null;
    }

    public PlaceConfig toPlaceConfig() {
        PlaceConfig placeConfig = new PlaceConfig().position(position).radius(radius);
        if (polygon2D != null && !polygon2D.isEmpty()) {
            placeConfig.setPolygon2D(new Polygon2D(polygon2D));
        }
        return placeConfig;
    }

    public void fromPlaceConfig(PlaceConfig placeConfig) {
        if (polygon2D == null) {
            polygon2D = new ArrayList<>();
        }
        polygon2D.clear();
        if (placeConfig.getPolygon2D() != null) {
            polygon2D.addAll(placeConfig.getPolygon2D().getCorners());
        }
        position = placeConfig.getPosition();
        radius = placeConfig.getRadius();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlaceConfigEntity that = (PlaceConfigEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
