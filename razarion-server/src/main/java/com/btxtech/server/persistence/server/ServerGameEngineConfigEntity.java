package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
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
    @ElementCollection
    @CollectionTable(name = "SERVER_GAME_ENGINE_START_REGION", joinColumns = @JoinColumn(name = "serverGameEngineId"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> startRegion;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverGameEngineId", nullable = false)
    private List<ServerResourceRegionConfigEntity> resourceRegionConfigs;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
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

    public SlavePlanetConfig getSlavePlanetConfig() {
        Polygon2D polygon2D = null;
        if (startRegion != null && !startRegion.isEmpty()) {
            polygon2D = new Polygon2D(startRegion);
        }
        return new SlavePlanetConfig().setStartRegion(polygon2D);
    }

    public void setStartRegion(List<DecimalPosition> startRegion) {
        this.startRegion.clear();
        this.startRegion.addAll(startRegion);
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
