package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.StartRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

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
import java.util.Iterator;
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
    private List<StartRegionLevelConfigEntity> startRegionLevelConfigs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "SERVER_GAME_ENGINE_BOT_CONFIG",
            joinColumns = @JoinColumn(name = "serverGameEngineId"),
            inverseJoinColumns = @JoinColumn(name = "botConfigId"))
    private List<BotConfigEntity> botConfigs;

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
        Collection<BotConfig> botConfigs = new ArrayList<>();
        if (this.botConfigs == null) {
            return botConfigs;
        }
        for (BotConfigEntity botConfigEntity : this.botConfigs) {
            botConfigs.add(botConfigEntity.toBotConfig());
        }
        return botConfigs;
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

    public void setBotConfigs(ItemTypePersistence itemTypePersistence, List<BotConfig> botConfigs) {
        this.botConfigs.clear();
        for (BotConfig botConfig : botConfigs) {
            BotConfigEntity botConfigEntity = new BotConfigEntity();
            botConfigEntity.fromBotConfig(itemTypePersistence, botConfig);
            this.botConfigs.add(botConfigEntity);
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
