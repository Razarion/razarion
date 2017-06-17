package com.btxtech.server.persistence.surface;

import com.btxtech.shared.dto.TerrainSlopePosition;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 09.05.2016.
 */
@Entity
@Table(name = "TERRAIN_SLOPE_POSITION")
public class TerrainSlopePositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn
    private SlopeConfigEntity slopeConfigEntity;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, name = "terrainSlopePositionId")
    @OrderColumn(name = "orderColumn")
    private List<TerrainSlopeCornerEntity> polygon;


    public Integer getId() {
        return id;
    }

    public TerrainSlopePosition toTerrainSlopePosition() {
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition().setId(id).setSlopeConfigId(slopeConfigEntity.getId());
        return terrainSlopePosition.setPolygon(polygon.stream().map(TerrainSlopeCornerEntity::toTerrainSlopeCorner).collect(Collectors.toList()));
    }

    public void setSlopeConfigEntity(SlopeConfigEntity slopeConfigEntity) {
        this.slopeConfigEntity = slopeConfigEntity;
    }

    public void setPolygon(List<TerrainSlopeCornerEntity> polygon) {
        if (this.polygon == null) {
            this.polygon = new ArrayList<>();
        }
        this.polygon.clear();
        this.polygon.addAll(polygon);
    }

    public List<TerrainSlopeCornerEntity> getPolygon() {
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
