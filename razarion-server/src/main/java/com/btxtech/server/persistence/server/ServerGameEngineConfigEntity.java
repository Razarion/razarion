package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetCrudPersistence;
import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.bot.BotSceneConfigEntity;
import com.btxtech.server.persistence.itemtype.ResourceItemTypeCrudPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;
import static com.btxtech.server.persistence.PersistenceUtil.fromConfigs;
import static com.btxtech.server.persistence.PersistenceUtil.toConfigList;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Entity
@Table(name = "SERVER_GAME_ENGINE_CONFIG")
public class ServerGameEngineConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
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
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "SERVER_GAME_ENGINE_BOT_CONFIG",
            joinColumns = @JoinColumn(name = "serverGameEngineId"),
            inverseJoinColumns = @JoinColumn(name = "botConfigId"))
    private List<BotConfigEntity> botConfigs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "SERVER_GAME_ENGINE_BOT_SCENE_CONFIG",
            joinColumns = @JoinColumn(name = "serverGameEngineId"),
            inverseJoinColumns = @JoinColumn(name = "botSceneConfigId"))
    private List<BotSceneConfigEntity> botSceneConfigs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "serverGameEngineConfig")
    private List<ServerLevelQuestEntity> serverQuestEntities;

    public ServerGameEngineConfig toServerGameEngineConfig() {
        return new ServerGameEngineConfig()
                .id(id)
                .internalName(internalName)
                .resourceRegionConfigs(toConfigList(resourceRegionConfigs, ServerResourceRegionConfigEntity::toResourceRegionConfig))
                .startRegionConfigs(toConfigList(startRegionConfigs, StartRegionConfigEntity::toStartRegionConfig))
                .planetConfigId(extractId(planetEntity, PlanetEntity::getId));
    }

    public void fromServerGameEngineConfig(ServerGameEngineConfig config, PlanetCrudPersistence planetCrudPersistence, ResourceItemTypeCrudPersistence resourceItemTypeCrudPersistence, LevelCrudPersistence levelCrudPersistence) {
        internalName = config.getInternalName();
        planetEntity = planetCrudPersistence.getEntity(config.getPlanetConfigId());
        resourceRegionConfigs = fromConfigs(resourceRegionConfigs,
                config.getResourceRegionConfigs(),
                ServerResourceRegionConfigEntity::new,
                (serverResourceRegionConfigEntity, resourceRegionConfig) -> serverResourceRegionConfigEntity.fromResourceRegionConfig(resourceItemTypeCrudPersistence, resourceRegionConfig));
        startRegionConfigs = fromConfigs(startRegionConfigs,
                config.getStartRegionConfigs(),
                StartRegionConfigEntity::new,
                (startRegionConfigEntity, startRegionConfig) -> startRegionConfigEntity.fromStartRegionConfig(startRegionConfig, levelCrudPersistence));
    }

    public Integer getId() {
        return id;
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

    public Collection<BotConfig> getBotConfigs() {
        if (this.botConfigs == null) {
            return Collections.emptyList();
        }
        return this.botConfigs.stream().map(BotConfigEntity::toBotConfig).collect(Collectors.toList());
    }

    public Collection<BotSceneConfig> getBotSceneConfigs() {
        if (this.botSceneConfigs == null) {
            return Collections.emptyList();
        }
        return this.botSceneConfigs.stream().map(BotSceneConfigEntity::toBotSceneConfig).collect(Collectors.toList());
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

    public void setPlanetEntity(PlanetEntity planetEntity) {
        this.planetEntity = planetEntity;
    }

    public Polygon2D findStartRegion(int levelNumber) {
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
        return result.getStartRegion();
    }

    public List<ServerLevelQuestEntity> getServerQuestEntities() {
        return serverQuestEntities;
    }

    public void setServerQuestEntities(List<ServerLevelQuestEntity> serverQuestEntities) {
        this.serverQuestEntities = serverQuestEntities;
    }

    public List<ServerResourceRegionConfigEntity> getResourceRegionConfigs() {
        return resourceRegionConfigs;
    }

    public void setResourceRegionConfigs(List<ServerResourceRegionConfigEntity> resourceRegionConfigs) {
        this.resourceRegionConfigs = resourceRegionConfigs;
    }

    public List<BotConfigEntity> getBotConfigEntities() {
        return botConfigs;
    }

    public void setBotConfigEntities(List<BotConfigEntity> botConfigs) {
        this.botConfigs = botConfigs;
    }

    public List<BotSceneConfigEntity> getBotSceneConfigEntities() {
        return botSceneConfigs;
    }

    public void setBotSceneConfigEntities(List<BotSceneConfigEntity> botSceneConfigs) {
        this.botSceneConfigs = botSceneConfigs;
    }

    public List<ServerBoxRegionConfigEntity> getServerBoxRegionConfigEntities() {
        return boxRegionConfigs;
    }

    public void setServerBoxRegionConfigEntities(List<ServerBoxRegionConfigEntity> boxRegionConfigs) {
        this.boxRegionConfigs = boxRegionConfigs;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
