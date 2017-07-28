package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 27.07.2017.
 */
@Entity
@Table(name = "SERVER_START_REGION_LEVEL_CONFIG")
public class StartRegionLevelConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ElementCollection
    @CollectionTable(name = "SERVER_START_REGION_LEVEL_CONFIG_POLYGON", joinColumns = @JoinColumn(name = "serverEngineLevelConfigId"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> startRegion;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;

    public Polygon2D getStartRegion() {
        if (startRegion != null && !startRegion.isEmpty()) {
            return new Polygon2D(startRegion);
        } else {
            return null;
        }
    }

    public void setStartRegion(List<DecimalPosition> startRegion) {
        if (this.startRegion == null) {
            this.startRegion = new ArrayList<>();
        } else {
            this.startRegion.clear();
        }
        if (startRegion != null) {
            this.startRegion.addAll(startRegion);
        }
    }

    public LevelEntity getMinimalLevel() {
        return minimalLevel;
    }

    public void setMinimalLevel(LevelEntity minimalLevel) {
        this.minimalLevel = minimalLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartRegionLevelConfigEntity that = (StartRegionLevelConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
