package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;

import java.util.Map;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class PlanetConfig implements Config {
    private int id;
    private String internalName;
    private Rectangle terrainTileDimension; // In terrain tiles TerrainUtil.TERRAIN_TILE_NODES_COUNT
    private Rectangle2D playGround;
    private Map<Integer, Integer> itemTypeLimitation;
    private int houseSpace;
    private int startRazarion;
    private int startBaseItemTypeId;
    private Integer groundConfigId;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    public Rectangle getTerrainTileDimension() {
        return terrainTileDimension;
    }

    public void setTerrainTileDimension(Rectangle terrainTileDimension) {
        this.terrainTileDimension = terrainTileDimension;
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

    public Integer getGroundConfigId() {
        return groundConfigId;
    }

    public PlanetConfig setGroundConfigId(Integer groundConfigId) {
        this.groundConfigId = groundConfigId;
        return this;
    }

    public PlanetConfig id(int id) {
        this.id = id;
        return this;
    }


    public PlanetConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }
}
