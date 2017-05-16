package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 07.05.2017.
 */
@Entity
@Table(name = "PLACE_CONFIG")
public class PlaceConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ElementCollection
    @CollectionTable(name = "PLACE_CONFIG_POSITION_POLYGON", joinColumns = @JoinColumn(name = "OWNER_ID"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> polygon2D;
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "x", column = @Column), @AttributeOverride(name = "y", column = @Column)})
    private DecimalPosition position;
    private Double radius;

    public PlaceConfig toPlaceConfig() {
        PlaceConfig placeConfig = new PlaceConfig().setPosition(position).setRadius(radius);
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
