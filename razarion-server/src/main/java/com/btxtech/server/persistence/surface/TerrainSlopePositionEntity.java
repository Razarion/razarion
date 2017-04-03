package com.btxtech.server.persistence.surface;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 09.05.2016.
 */
@Entity
@Table(name = "TERRAIN_SLOPE_POSITION")
public class TerrainSlopePositionEntity {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private SlopeConfigEntity slopeConfigEntity;
    @ElementCollection
    @CollectionTable(name = "TERRAIN_SLOPE_POSITION_POLYGON", joinColumns = @JoinColumn(name = "OWNER_ID"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> polygon;

    public Long getId() {
        return id;
    }

    public TerrainSlopePosition toTerrainSlopePosition() {
        return new TerrainSlopePosition().setId(id.intValue()).setSlopeConfigEntity(slopeConfigEntity.getId().intValue()).setPolygon(new ArrayList<>(polygon));
    }

    public void setSlopeConfigEntity(SlopeConfigEntity slopeConfigEntity) {
        this.slopeConfigEntity = slopeConfigEntity;
    }

    public void setPolygon(List<DecimalPosition> polygon) {
        this.polygon = polygon;
    }

    public List<DecimalPosition> getPolygon() {
        return polygon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainSlopePositionEntity that = (TerrainSlopePositionEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
