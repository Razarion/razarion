package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import static com.btxtech.server.service.PersistenceUtil.extractId;


/**
 * Created by Beat
 * on 27.07.2017.
 */
@Entity
@Table(name = "SERVER_START_REGION_CONFIG")
public class StartRegionConfigEntity extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity startRegion;
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

    public PlaceConfig getStartRegion() {
        if (startRegion != null) {
            return startRegion.toPlaceConfig();
        } else {
            return null;
        }
    }

    public DecimalPosition getNoBaseViewPosition() {
        return noBaseViewPosition;
    }

    public StartRegionConfig toStartRegionConfig() {
        return new StartRegionConfig()
                .id(getId())
                .internalName(getInternalName())
                .minimalLevelId(extractId(minimalLevel, LevelEntity::getId))
                .region(getStartRegion())
                .noBaseViewPosition(noBaseViewPosition);
    }

    public StartRegionConfigEntity fromStartRegionConfig(StartRegionConfig startRegionConfig) {
        setInternalName(startRegionConfig.getInternalName());
        minimalLevel = (LevelEntity) new LevelEntity().id(startRegionConfig.getMinimalLevelId());
        if (startRegionConfig.getRegion() != null) {
            startRegion = new PlaceConfigEntity();
            startRegion.fromPlaceConfig(startRegionConfig.getRegion());
        } else {
            startRegion = null;
        }
        noBaseViewPosition = startRegionConfig.getNoBaseViewPosition();
        return this;
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
