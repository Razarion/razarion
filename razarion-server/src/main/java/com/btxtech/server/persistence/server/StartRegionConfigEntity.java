package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.StartRegionConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
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

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * on 27.07.2017.
 */
@Entity
@Table(name = "SERVER_START_REGION_CONFIG")
public class StartRegionConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @ElementCollection
    @CollectionTable(name = "SERVER_START_REGION_CONFIG_POLYGON", joinColumns = @JoinColumn(name = "startRegionConfigEntityId"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> startRegion;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "noBaseViewPositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "noBaseViewPositionY")),
    })
    private DecimalPosition noBaseViewPosition;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;

    public LevelEntity getMinimalLevel() {
        return minimalLevel;
    }

    public Polygon2D getStartRegion() {
        if (startRegion != null && !startRegion.isEmpty()) {
            return new Polygon2D(startRegion);
        } else {
            return null;
        }
    }

    public DecimalPosition getNoBaseViewPosition() {
        return noBaseViewPosition;
    }

    public StartRegionConfig toStartRegionConfig() {
        return new StartRegionConfig()
                .id(id)
                .internalName(internalName)
                .minimalLevelId(extractId(minimalLevel, LevelEntity::getId))
                .region(getStartRegion())
                .noBaseViewPosition(noBaseViewPosition);
    }

    public void fromStartRegionConfig(StartRegionConfig startRegionConfig, LevelCrudPersistence levelCrudPersistence) {
        internalName = startRegionConfig.getInternalName();
        minimalLevel = levelCrudPersistence.getEntity(startRegionConfig.getMinimalLevelId());
        if (this.startRegion == null) {
            this.startRegion = new ArrayList<>();
        } else {
            this.startRegion.clear();
        }
        if (startRegionConfig.getRegion() != null) {
            this.startRegion.addAll(startRegionConfig.getRegion().getCorners());
        }
        noBaseViewPosition = startRegionConfig.getNoBaseViewPosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartRegionConfigEntity that = (StartRegionConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
