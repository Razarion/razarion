package com.btxtech.server.persistence.surface;

import com.btxtech.shared.datatypes.Index;
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
    private List<Index> polygon;

    public SlopeConfigEntity getSlopeConfigEntity() {
        return slopeConfigEntity;
    }

    public void setSlopeConfigEntity(SlopeConfigEntity slopeConfigEntity) {
        this.slopeConfigEntity = slopeConfigEntity;
    }

    public void setPolygon(List<Index> polygon) {
        if(this.polygon != null) {
            this.polygon.clear();
        } else {
            this.polygon = new ArrayList<>();
        }
        this.polygon.addAll(polygon);
    }

    public TerrainSlopePosition toTerrainSlopePosition() {
        return new TerrainSlopePosition(id.intValue(), slopeConfigEntity.getId().intValue(), new ArrayList<>(polygon));
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
