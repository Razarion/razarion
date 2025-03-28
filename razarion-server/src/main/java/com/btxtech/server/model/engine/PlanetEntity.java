package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.btxtech.server.service.PersistenceUtil.extractItemTypeLimitation;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "PLANET")
public class PlanetEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private GroundConfigEntity groundConfig;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "planet")
    private List<TerrainObjectPositionEntity> terrainObjectPositionEntities;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "width")),
            @AttributeOverride(name = "y", column = @Column(name = "height")),
    })
    private DecimalPosition size;
    @ElementCollection
    @MapKeyJoinColumn(name = "baseItemTypeEntityId")
    @CollectionTable(name = "PLANET_LIMITATION")
    private Map<BaseItemTypeEntity, Integer> itemTypeLimitation;
    private int houseSpace;
    private int startRazarion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity startBaseItemType;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] miniMapImage;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] compressedHeightMap;

    public PlanetConfig toPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig()
                .id(getId())
                .internalName(getInternalName())
                .size(size)
                .houseSpace(houseSpace)
                .startRazarion(startRazarion);
        if (groundConfig != null) {
            planetConfig.setGroundConfigId(groundConfig.getId());
        }
        planetConfig.setItemTypeLimitation(extractItemTypeLimitation(itemTypeLimitation));
        if (startBaseItemType != null) {
            planetConfig.setStartBaseItemTypeId(startBaseItemType.getId());
        }
        return planetConfig;
    }

    public void fromPlanetConfig(PlanetConfig planetConfig, GroundConfigEntity groundConfig, BaseItemTypeEntity startBaseItemType, Map<BaseItemTypeEntity, Integer> itemTypeLimitation) {
        setInternalName(planetConfig.getInternalName());
        this.groundConfig = groundConfig;
        houseSpace = planetConfig.getHouseSpace();
        startRazarion = planetConfig.getStartRazarion();
        size = planetConfig.getSize();
        this.startBaseItemType = startBaseItemType;
        if (this.itemTypeLimitation == null) {
            this.itemTypeLimitation = new HashMap<>();
        }
        this.itemTypeLimitation.clear();
        this.itemTypeLimitation.putAll(itemTypeLimitation);
    }

    public List<TerrainObjectPositionEntity> getTerrainObjectPositionEntities() {
        if (terrainObjectPositionEntities == null) {
            terrainObjectPositionEntities = new ArrayList<>();
        }
        return terrainObjectPositionEntities;
    }

    public byte[] getMiniMapImage() {
        return miniMapImage;
    }

    public void setMiniMapImage(byte[] miniMapImage) {
        this.miniMapImage = miniMapImage;
    }

    public byte[] getCompressedHeightMap() {
        return compressedHeightMap;
    }

    public void setCompressedHeightMap(byte[] compressedHeightMap) {
        this.compressedHeightMap = compressedHeightMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlanetEntity that = (PlanetEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
