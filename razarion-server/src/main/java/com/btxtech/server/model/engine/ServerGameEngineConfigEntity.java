package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;
import static com.btxtech.server.service.PersistenceUtil.toConfigList;

@Entity
@Table(name = "SERVER_GAME_ENGINE_CONFIG")
public class ServerGameEngineConfigEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private PlanetEntity planetEntity;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    @JsonIgnore
    private List<ServerResourceRegionConfigEntity> resourceRegionConfigs;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    @JsonIgnore
    private List<ServerBoxRegionConfigEntity> boxRegionConfigs;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    @JsonIgnore
    private List<StartRegionConfigEntity> startRegionConfigs;
    // TODO BotConfigEntity in "BOT_CONFIG" table not getting removed if an entity from this list is removed. Same in SceneEntity
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "SERVER_GAME_ENGINE_BOT_CONFIG",
            joinColumns = @JoinColumn(name = "serverGameEngineId"),
            inverseJoinColumns = @JoinColumn(name = "botConfigId"))
    @JsonIgnore
    private List<BotConfigEntity> botConfigs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "serverGameEngineConfig")
    @JsonIgnore
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

    public void setBoxRegionConfigs(List<ServerBoxRegionConfigEntity> boxRegionConfigs) {
        this.boxRegionConfigs.clear();
        this.boxRegionConfigs.addAll(boxRegionConfigs);
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
        int spawnPointCount = 13;
        double horizontalDistance = 10;
        double startX = 178;
        double startY = 16;
        var positions = new ArrayList<DecimalPosition>();
        for (int i = 0; i < spawnPointCount; i++) {
            positions.add(new DecimalPosition(startX - (i * horizontalDistance), startY));
        }
        return new SlavePlanetConfig()
                .startRegion(result.getStartRegion())
                .findFreePosition(true)
                .positionRadius(result.getPositionRadius())
                .positionMaxItems(result.getPositionMaxItems())
                .positionPath(positions);
    }

    @JsonGetter("planetConfigId")
    public Integer getJsonPlanetConfigId() {
        return planetEntity != null ? planetEntity.getId() : null;
    }

    @JsonGetter("resourceRegionConfigs")
    public List<ResourceRegionConfig> getJsonResourceRegionConfigs() {
        return toConfigList(resourceRegionConfigs, ServerResourceRegionConfigEntity::toResourceRegionConfig);
    }

    @JsonGetter("boxRegionConfigs")
    public List<BoxRegionConfig> getJsonBoxRegionConfigs() {
        return toConfigList(boxRegionConfigs, ServerBoxRegionConfigEntity::toBoxRegionConfig);
    }

    @JsonGetter("startRegionConfigs")
    public List<StartRegionConfig> getJsonStartRegionConfigs() {
        if (startRegionConfigs == null) {
            return null;
        }
        return startRegionConfigs.stream()
                .map(StartRegionConfigEntity::toStartRegionConfig)
                .toList();
    }

    @JsonGetter("botConfigs")
    public List<BotConfig> getJsonBotConfigs() {
        return toConfigList(botConfigs, BotConfigEntity::toBotConfig);
    }

    @JsonGetter("serverLevelQuestConfigs")
    public List<ServerLevelQuestConfig> getJsonServerLevelQuestConfigs() {
        return toConfigList(serverLevelQuestEntities, ServerLevelQuestEntity::toServerLevelQuestConfig);
    }

    public void setStartRegionConfigs(List<StartRegionConfigEntity> startRegionConfigs) {
        this.startRegionConfigs.clear();
        this.startRegionConfigs.addAll(startRegionConfigs);
    }

    public void setResourceRegionConfigs(List<ServerResourceRegionConfigEntity> resourceRegionConfigs) {
        this.resourceRegionConfigs.clear();
        this.resourceRegionConfigs.addAll(resourceRegionConfigs);
    }

    public void setBotConfigs(List<BotConfigEntity> botConfigs) {
        this.botConfigs.clear();
        this.botConfigs.addAll(botConfigs);
    }

    public List<ServerLevelQuestEntity> getServerLevelQuestEntities() {
        return serverLevelQuestEntities;
    }

    public void setPlanetEntity(PlanetEntity planetEntity) {
        this.planetEntity = planetEntity;
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
