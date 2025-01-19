package com.btxtech.server.persistence;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "PLANET")
public class PlanetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
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

    public Integer getId() {
        return id;
    }

    public PlanetConfig toPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig()
                .id(id)
                .internalName(internalName)
                .size(size)
                .houseSpace(houseSpace)
                .startRazarion(startRazarion);
        if (groundConfig != null) {
            planetConfig.setGroundConfigId(groundConfig.getId());
        }
        planetConfig.setItemTypeLimitation(PersistenceUtil.extractItemTypeLimitation(itemTypeLimitation));
        if (startBaseItemType != null) {
            planetConfig.setStartBaseItemTypeId(startBaseItemType.getId());
        }
        return planetConfig;
    }

    public void fromPlanetConfig(PlanetConfig planetConfig, GroundConfigEntity groundConfig, BaseItemTypeEntity startBaseItemType, Map<BaseItemTypeEntity, Integer> itemTypeLimitation) {
        internalName = planetConfig.getInternalName();
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
