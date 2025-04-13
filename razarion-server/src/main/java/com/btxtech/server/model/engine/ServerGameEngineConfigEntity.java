package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.dto.*;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;
import static com.btxtech.server.service.PersistenceUtil.toConfigList;

@Entity
@Table(name = "SERVER_GAME_ENGINE_CONFIG")
public class ServerGameEngineConfigEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    private PlanetEntity planetEntity;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    private List<ServerResourceRegionConfigEntity> resourceRegionConfigs;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    private List<ServerBoxRegionConfigEntity> boxRegionConfigs;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    private List<StartRegionConfigEntity> startRegionConfigs;
    // TODO BotConfigEntity in "BOT_CONFIG" table not getting removed if an entity from this list is removed. Same in SceneEntity
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "SERVER_GAME_ENGINE_BOT_CONFIG",
            joinColumns = @JoinColumn(name = "serverGameEngineId"),
            inverseJoinColumns = @JoinColumn(name = "botConfigId"))
    private List<BotConfigEntity> botConfigs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "serverGameEngineConfig")
    private List<ServerLevelQuestEntity> serverLevelQuestEntities;

    public ServerGameEngineConfig toServerGameEngineConfig() {
        return new ServerGameEngineConfig()
                .id(getId())
                .internalName(getInternalName())
                .planetConfigId(extractId(planetEntity, PlanetEntity::getId))
                .resourceRegionConfigs(toConfigList(resourceRegionConfigs, ServerResourceRegionConfigEntity::toResourceRegionConfig))
                .startRegionConfigs(toConfigList(startRegionConfigs, StartRegionConfigEntity::toStartRegionConfig))
                .botConfigs(toConfigList(botConfigs, BotConfigEntity::toBotConfig))
                .serverLevelQuestConfig(toConfigList(serverLevelQuestEntities, ServerLevelQuestEntity::toServerLevelQuestConfig))
                .boxRegionConfigs(toConfigList(boxRegionConfigs, ServerBoxRegionConfigEntity::toBoxRegionConfig));
    }

    public PlanetConfig getPlanetConfig() {
        if (planetEntity != null) {
            return planetEntity.toPlanetConfig();
        } else {
            return null;
        }
    }

    public MasterPlanetConfig getMasterPlanetConfig() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        if (this.resourceRegionConfigs != null) {
            for (ServerResourceRegionConfigEntity resourceRegionConfig : this.resourceRegionConfigs) {
                resourceRegionConfigs.add(resourceRegionConfig.toResourceRegionConfig());
            }
        }
        return new MasterPlanetConfig().setResourceRegionConfigs(resourceRegionConfigs);
    }

    public Collection<BoxRegionConfig> getBoxRegionConfigs() {
        Collection<BoxRegionConfig> boxRegionConfigs = new ArrayList<>();
        if (this.boxRegionConfigs == null) {
            return boxRegionConfigs;
        }
        for (ServerBoxRegionConfigEntity serverBoxRegionConfigEntity : this.boxRegionConfigs) {
            boxRegionConfigs.add(serverBoxRegionConfigEntity.toBoxRegionConfig());
        }
        return boxRegionConfigs;
    }

    public SlavePlanetConfig findSlavePlanetConfig4Level(int levelNumber) {
        if (startRegionConfigs == null) {
            return null;
        }
        Integer bestLevelNumber = null;
        StartRegionConfigEntity result = null;
        for (StartRegionConfigEntity startRegionLevelConfigEntity : startRegionConfigs) {
            if (startRegionLevelConfigEntity.getMinimalLevel() != null && startRegionLevelConfigEntity.getStartRegion() != null && levelNumber >= startRegionLevelConfigEntity.getMinimalLevel().getNumber()) {
                if (bestLevelNumber == null || bestLevelNumber < startRegionLevelConfigEntity.getMinimalLevel().getNumber()) {
                    bestLevelNumber = startRegionLevelConfigEntity.getMinimalLevel().getNumber();
                    result = startRegionLevelConfigEntity;
                }
            }
        }
        if (result == null) {
            return null;
        }
        return new SlavePlanetConfig()
                .startRegion(result.getStartRegion())
                .noBaseViewPosition(result.getNoBaseViewPosition());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerGameEngineConfigEntity that = (ServerGameEngineConfigEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
