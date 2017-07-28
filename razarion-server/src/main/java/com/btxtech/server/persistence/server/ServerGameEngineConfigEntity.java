package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
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

    public void setStartRegion(LevelEntity levelEntity, List<DecimalPosition> startRegion) {
        StartRegionLevelConfigEntity startRegionLevelConfigEntity = null;
        if (startRegionLevelConfigs == null) {
            startRegionLevelConfigs = new ArrayList<>();
            startRegionLevelConfigEntity = new StartRegionLevelConfigEntity();
            startRegionLevelConfigEntity.setMinimalLevel(levelEntity);
            startRegionLevelConfigs.add(startRegionLevelConfigEntity);
        } else {
            for (StartRegionLevelConfigEntity current : startRegionLevelConfigs) {
                if (current.getMinimalLevel().equals(levelEntity)) {
                    startRegionLevelConfigEntity = current;
                    break;
                }
            }
            if (startRegionLevelConfigEntity == null) {
                startRegionLevelConfigEntity = new StartRegionLevelConfigEntity();
                startRegionLevelConfigEntity.setMinimalLevel(levelEntity);
                startRegionLevelConfigs.add(startRegionLevelConfigEntity);
            }
        }
        startRegionLevelConfigEntity.setStartRegion(startRegion);
    }

    public void clearStartRegion(LevelEntity levelEntity) {
        if (startRegionLevelConfigs == null) {
            return;
        }
        for (Iterator<StartRegionLevelConfigEntity> iterator = startRegionLevelConfigs.iterator(); iterator.hasNext(); ) {
            StartRegionLevelConfigEntity current = iterator.next();
            if (current.getMinimalLevel().equals(levelEntity)) {
                iterator.remove();
                return;
            }
        }
    }

    public Polygon2D findStartRegion(int levelNumber) {
        if (startRegionLevelConfigs == null) {
            return null;
        }
        Integer bestLevelNumber = null;
        StartRegionLevelConfigEntity result = null;
        for (StartRegionLevelConfigEntity startRegionLevelConfigEntity : startRegionLevelConfigs) {
            if (startRegionLevelConfigEntity.getStartRegion() != null && levelNumber >= startRegionLevelConfigEntity.getMinimalLevel().getNumber()) {
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
