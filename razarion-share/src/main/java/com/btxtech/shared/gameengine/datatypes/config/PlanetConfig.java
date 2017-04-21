package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class PlanetConfig {
    private int planetId;
    private GameEngineMode gameEngineMode;
    private Rectangle groundMeshDimension;
    private List<TerrainSlopePosition> terrainSlopePositions;
    private List<TerrainObjectPosition> terrainObjectPositions;
    private Map<Integer, Integer> itemTypeLimitation;
    private double waterLevel;
    private int houseSpace;
    private Integer actualBaseId;
    private int startRazarion;
    private int startBaseItemTypeId;
    private Rectangle2D playGround;
    private List<SyncBaseItemInfo> syncBaseItemInfos;
    private List<PlayerBaseInfo> playerBaseInfos;
    private List<BotConfig> botConfigs;

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }

    public PlanetConfig setGameEngineMode(GameEngineMode gameEngineMode) {
        this.gameEngineMode = gameEngineMode;
        return this;
    }

    public Rectangle getGroundMeshDimension() {
        return groundMeshDimension;
    }

    public PlanetConfig setGroundMeshDimension(Rectangle groundMeshDimension) {
        this.groundMeshDimension = groundMeshDimension;
        return this;
    }

    public List<TerrainSlopePosition> getTerrainSlopePositions() {
        return terrainSlopePositions;
    }

    public PlanetConfig setTerrainSlopePositions(List<TerrainSlopePosition> terrainSlopePositions) {
        this.terrainSlopePositions = terrainSlopePositions;
        return this;
    }

    public List<TerrainObjectPosition> getTerrainObjectPositions() {
        return terrainObjectPositions;
    }

    public PlanetConfig setTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions) {
        this.terrainObjectPositions = terrainObjectPositions;
        return this;
    }

    public Map<Integer, Integer> getItemTypeLimitation() {
        return itemTypeLimitation;
    }

    public PlanetConfig setItemTypeLimitation(Map<Integer, Integer> itemTypeLimitation) {
        this.itemTypeLimitation = itemTypeLimitation;
        return this;
    }

    public int imitation4ItemType(int itemTypeId) {
        Integer limitation = itemTypeLimitation.get(itemTypeId);
        if (limitation != null) {
            return limitation;
        } else {
            return 0;
        }
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public PlanetConfig setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
        return this;
    }

    public int getHouseSpace() {
        return houseSpace;
    }

    public PlanetConfig setHouseSpace(int houseSpace) {
        this.houseSpace = houseSpace;
        return this;
    }

    public int getStartBaseItemTypeId() {
        return startBaseItemTypeId;
    }

    public PlanetConfig setStartBaseItemTypeId(int startBaseItemTypeId) {
        this.startBaseItemTypeId = startBaseItemTypeId;
        return this;
    }

    public int getStartRazarion() {
        return startRazarion;
    }

    public PlanetConfig setStartRazarion(int startRazarion) {
        this.startRazarion = startRazarion;
        return this;
    }

    public Rectangle2D getPlayGround() {
        return playGround;
    }

    public PlanetConfig setPlayGround(Rectangle2D playGround) {
        this.playGround = playGround;
        return this;
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return syncBaseItemInfos;
    }

    public PlanetConfig setSyncBaseItemInfos(List<SyncBaseItemInfo> syncBaseItemInfos) {
        this.syncBaseItemInfos = syncBaseItemInfos;
        return this;
    }

    public List<PlayerBaseInfo> getPlayerBaseInfos() {
        return playerBaseInfos;
    }

    public PlanetConfig setPlayerBaseInfos(List<PlayerBaseInfo> playerBaseInfos) {
        this.playerBaseInfos = playerBaseInfos;
        return this;
    }

    public List<BotConfig> getBotConfigs() {
        return botConfigs;
    }

    public PlanetConfig setBotConfigs(List<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
        return this;
    }

    public Integer getActualBaseId() {
        return actualBaseId;
    }

    public void setActualBaseId(Integer actualBase) {
        this.actualBaseId = actualBase;
    }
}
