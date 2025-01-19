package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;
import org.dominokit.jackson.annotation.JSONMapper;

import java.util.Map;

/**
 * Created by Beat
 * 05.07.2016.
 */
@JsType
@JSONMapper
public class PlanetConfig implements Config {
    private int id;
    private String internalName;
    private DecimalPosition size;
    private Map<Integer, Integer> itemTypeLimitation;
    private int houseSpace;
    private int startRazarion;
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private Integer startBaseItemTypeId;
    @CollectionReference(CollectionReferenceType.GROUND)
    private Integer groundConfigId;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public @Nullable DecimalPosition getSize() {
        return size;
    }

    public void setSize(@Nullable DecimalPosition size) {
        this.size = size;
    }

    public Map<Integer, Integer> getItemTypeLimitation() {
        return itemTypeLimitation;
    }

    public void setItemTypeLimitation(Map<Integer, Integer> itemTypeLimitation) {
        this.itemTypeLimitation = itemTypeLimitation;
    }

    public int getHouseSpace() {
        return houseSpace;
    }

    public void setHouseSpace(int houseSpace) {
        this.houseSpace = houseSpace;
    }

    public int getStartRazarion() {
        return startRazarion;
    }

    public void setStartRazarion(int startRazarion) {
        this.startRazarion = startRazarion;
    }

    public @Nullable Integer getStartBaseItemTypeId() {
        return startBaseItemTypeId;
    }

    public void setStartBaseItemTypeId(@Nullable Integer startBaseItemTypeId) {
        this.startBaseItemTypeId = startBaseItemTypeId;
    }

    public @Nullable Integer getGroundConfigId() {
        return groundConfigId;
    }

    public void setGroundConfigId(@Nullable Integer groundConfigId) {
        this.groundConfigId = groundConfigId;
    }

    public int imitation4ItemType(int itemTypeId) {
        Integer limitation = itemTypeLimitation.get(itemTypeId);
        if (limitation != null) {
            return limitation;
        } else {
            return 0;
        }
    }

    public PlanetConfig id(int id) {
        setId(id);
        return this;
    }

    public PlanetConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public PlanetConfig size(DecimalPosition size) {
        setSize(size);
        return this;
    }

    public PlanetConfig itemTypeLimitation(Map<Integer, Integer> itemTypeLimitation) {
        setItemTypeLimitation(itemTypeLimitation);
        return this;
    }

    public PlanetConfig houseSpace(int houseSpace) {
        setHouseSpace(houseSpace);
        return this;
    }

    public PlanetConfig startRazarion(int startRazarion) {
        setStartRazarion(startRazarion);
        return this;
    }

    public PlanetConfig startBaseItemTypeId(Integer startBaseItemTypeId) {
        setStartBaseItemTypeId(startBaseItemTypeId);
        return this;
    }

    public PlanetConfig groundConfigId(Integer groundConfigId) {
        setGroundConfigId(groundConfigId);
        return this;
    }
}
