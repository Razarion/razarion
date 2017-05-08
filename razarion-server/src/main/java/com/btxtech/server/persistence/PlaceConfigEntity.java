package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by Beat
 * 07.05.2017.
 */
@Entity
@Table(name = "PLACE_CONFIG")
public class PlaceConfigEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @ElementCollection
    @CollectionTable(name = "PLACE_CONFIG_POSITION_POLYGON", joinColumns = @JoinColumn(name = "OWNER_ID"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> polygon2D;
    @Embedded
    private DecimalPosition position;
    private Double radius;

    public PlaceConfig toPlaceConfig() {
        throw new UnsupportedOperationException("TODO");
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
