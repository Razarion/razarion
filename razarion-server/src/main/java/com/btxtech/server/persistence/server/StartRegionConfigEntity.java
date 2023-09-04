package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;

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
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity startRegion;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "noBaseViewPositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "noBaseViewPositionY")),
    })
    private DecimalPosition noBaseViewPosition;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;

    public Integer getId() {
        return id;
    }

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
                .id(id)
                .internalName(internalName)
                .minimalLevelId(extractId(minimalLevel, LevelEntity::getId))
                .region(getStartRegion())
                .noBaseViewPosition(noBaseViewPosition);
    }

    public void fromStartRegionConfig(StartRegionConfig startRegionConfig, LevelCrudPersistence levelCrudPersistence) {
        internalName = startRegionConfig.getInternalName();
        minimalLevel = levelCrudPersistence.getEntity(startRegionConfig.getMinimalLevelId());
        if (startRegionConfig.getRegion() != null) {
            startRegion = new PlaceConfigEntity();
            startRegion.fromPlaceConfig(startRegionConfig.getRegion());
        } else {
            startRegion = null;
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
