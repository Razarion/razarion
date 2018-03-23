package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.bot.BotSceneConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.BoxRegionConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.StartRegionConfig;
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
    @OneToOne
    private PlanetEntity planetEntity;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    private List<ServerResourceRegionConfigEntity> resourceRegionConfigs;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    private List<ServerBoxRegionConfigEntity> boxRegionConfigs;
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    private List<StartRegionLevelConfigEntity> startRegionLevelConfigs;
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

    public void setResourceRegionConfigs(ItemTypePersistence itemTypePersistence, List<ResourceRegionConfig> resourceRegionConfigs) {
        this.resourceRegionConfigs.clear();
        for (ResourceRegionConfig resourceRegionConfig : resourceRegionConfigs) {
            ServerResourceRegionConfigEntity serverResourceRegionConfigEntity = new ServerResourceRegionConfigEntity();
            serverResourceRegionConfigEntity.fromResourceRegionConfig(itemTypePersistence, resourceRegionConfig);
            this.resourceRegionConfigs.add(serverResourceRegionConfigEntity);
        }
    }

    public List<ObjectNameId> readStartRegionObjectNameIds() {
        if (startRegionLevelConfigs == null) {
            return new ArrayList<>();
        } else {
            return startRegionLevelConfigs.stream().map(StartRegionLevelConfigEntity::createObjectNameId).collect(Collectors.toList());
        }
    }

    public StartRegionConfig readStartRegionConfig(int id) {
        if (startRegionLevelConfigs != null) {
            return startRegionLevelConfigs.stream().filter(startRegionLevelConfigEntity -> id == startRegionLevelConfigEntity.getId()).findFirst().map(StartRegionLevelConfigEntity::toStartRegionConfig).orElseThrow(() -> new IllegalArgumentException("No StartRegionLevelConfigEntity for id: " + id + " on ServerGameEngineConfigEntity: " + this.id));
        } else {
            throw new IllegalArgumentException("No StartRegionLevelConfigEntity for id: " + id + " on ServerGameEngineConfigEntity: " + this.id);
        }
    }

    public StartRegionLevelConfigEntity createStartRegionConfig() {
        if (startRegionLevelConfigs == null) {
            startRegionLevelConfigs = new ArrayList<>();
        }
        StartRegionLevelConfigEntity startRegionLevelConfigEntity = new StartRegionLevelConfigEntity();
        startRegionLevelConfigs.add(startRegionLevelConfigEntity);
        return startRegionLevelConfigEntity;
    }

    public void updateStartRegionConfig(StartRegionConfig startRegionConfig, LevelPersistence levelPersistence) {
        if (startRegionLevelConfigs != null) {
            StartRegionLevelConfigEntity startRegionLevelConfigEntityDb = startRegionLevelConfigs.stream().filter(startRegionLevelConfigEntity -> startRegionConfig.getId() == startRegionLevelConfigEntity.getId()).findFirst().orElseThrow(() -> new IllegalArgumentException("No StartRegionLevelConfigEntity for id: " + id + " on ServerGameEngineConfigEntity: " + this.id));
            startRegionLevelConfigEntityDb.setMinimalLevel(levelPersistence.getLevel4Id(startRegionConfig.getMinimalLevelId()));
            startRegionLevelConfigEntityDb.setInternalName(startRegionConfig.getInternalName());
            if (startRegionConfig.getRegion() != null) {
                startRegionLevelConfigEntityDb.setStartRegion(startRegionConfig.getRegion().getCorners());
            } else {
                startRegionLevelConfigEntityDb.setStartRegion(null);
            }
        } else {
            throw new IllegalArgumentException("No StartRegionLevelConfigEntity for id: " + startRegionConfig.getId() + " on ServerGameEngineConfigEntity: " + this.id);
        }
    }

    public void deleteStartRegion(int id) {
        if (startRegionLevelConfigs != null) {
            startRegionLevelConfigs.removeIf(startRegionLevelConfigEntity -> id == startRegionLevelConfigEntity.getId());
        }
    }

    public Polygon2D findStartRegion(int levelNumber) {
        if (startRegionLevelConfigs == null) {
            return null;
        }
        Integer bestLevelNumber = null;
        StartRegionLevelConfigEntity result = null;
        for (StartRegionLevelConfigEntity startRegionLevelConfigEntity : startRegionLevelConfigs) {
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
