package com.btxtech.server.persistence;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
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
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "planet")
    private List<TerrainSlopePositionEntity> terrainSlopePositionEntities;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "planet")
    private List<TerrainObjectPositionEntity> terrainObjectPositionEntities;
    @AttributeOverrides({
            @AttributeOverride(name = "start.x", column = @Column(name = "groundMeshDimensionStartX")),
            @AttributeOverride(name = "start.y", column = @Column(name = "groundMeshDimensionStartY")),
            @AttributeOverride(name = "end.x", column = @Column(name = "groundMeshDimensionEndX")),
            @AttributeOverride(name = "end.y", column = @Column(name = "groundMeshDimensionEndY")),
    })
    private Rectangle groundMeshDimension;
    @AttributeOverrides({
            @AttributeOverride(name = "start.x", column = @Column(name = "playGroundStartX")),
            @AttributeOverride(name = "start.y", column = @Column(name = "playGroundStartY")),
            @AttributeOverride(name = "end.x", column = @Column(name = "playGroundEndX")),
            @AttributeOverride(name = "end.y", column = @Column(name = "playGroundEndY")),
    })
    private Rectangle2D playGround;
    @ElementCollection
    @MapKeyJoinColumn(name = "baseItemTypeEntityId")
    @CollectionTable(name = "PLANET_LIMITATION")
    private Map<BaseItemTypeEntity, Integer> itemTypeLimitation;
    private int houseSpace;
    private int startRazarion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity startBaseItemType;
    private double shadowRotationX;
    private double shadowRotationY;
    private double shadowAlpha;
    private double shape3DLightRotateX;
    private double shape3DLightRotateY;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] miniMapImage;

    public Integer getId() {
        return id;
    }

    public PlanetConfig toPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setPlanetId(id);
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        for (TerrainObjectPositionEntity terrainObjectPositionEntity : terrainObjectPositionEntities) {
            terrainObjectPositions.add(terrainObjectPositionEntity.toTerrainObjectPosition());
        }
        planetConfig.setTerrainObjectPositions(terrainObjectPositions);
        planetConfig.setTerrainTileDimension(groundMeshDimension);
        planetConfig.setPlayGround(playGround);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        if (this.itemTypeLimitation != null) {
            for (Map.Entry<BaseItemTypeEntity, Integer> entry : this.itemTypeLimitation.entrySet()) {
                itemTypeLimitation.put(entry.getKey().getId(), entry.getValue());
            }
        }
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        planetConfig.setHouseSpace(houseSpace).setStartRazarion(startRazarion);
        if (startBaseItemType != null) {
            planetConfig.setStartBaseItemTypeId(startBaseItemType.getId());
        }
        return planetConfig;
    }

    public PlanetVisualConfig toPlanetVisualConfig() {
        PlanetVisualConfig planetVisualConfig = new PlanetVisualConfig();
        planetVisualConfig.setShadowRotationX(shadowRotationX).setShadowRotationY(shadowRotationY).setShadowAlpha(shadowAlpha);
        planetVisualConfig.setShape3DLightRotateX(shape3DLightRotateX).setShape3DLightRotateY(shape3DLightRotateY);
        return planetVisualConfig;
    }

    public void fromPlanetVisualConfig(PlanetVisualConfig planetVisualConfig) {
        shadowRotationX = planetVisualConfig.getShadowRotationX();
        shadowRotationY = planetVisualConfig.getShadowRotationY();
        shadowAlpha = planetVisualConfig.getShadowAlpha();
        shape3DLightRotateX = planetVisualConfig.getShape3DLightRotateX();
        shape3DLightRotateY = planetVisualConfig.getShape3DLightRotateY();
    }

    public List<TerrainSlopePositionEntity> getTerrainSlopePositionEntities() {
        return terrainSlopePositionEntities;
    }

    public List<TerrainObjectPositionEntity> getTerrainObjectPositionEntities() {
        return terrainObjectPositionEntities;
    }

    public byte[] getMiniMapImage() {
        return miniMapImage;
    }

    public void setMiniMapImage(byte[] miniMapImage) {
        this.miniMapImage = miniMapImage;
    }

    public void setHouseSpace(int houseSpace) {
        this.houseSpace = houseSpace;
    }

    public void setStartRazarion(int startRazarion) {
        this.startRazarion = startRazarion;
    }

    public void setStartBaseItemType(BaseItemTypeEntity startBaseItemType) {
        this.startBaseItemType = startBaseItemType;
    }

    public Map<BaseItemTypeEntity, Integer> getItemTypeLimitation() {
        return itemTypeLimitation;
    }

    public void setItemTypeLimitation(Map<BaseItemTypeEntity, Integer> itemTypeLimitation) {
        this.itemTypeLimitation = itemTypeLimitation;
    }

    public void setPlayGround(Rectangle2D playGround) {
        this.playGround = playGround;
    }

    public void setGroundMeshDimension(Rectangle groundMeshDimension) {
        this.groundMeshDimension = groundMeshDimension;
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
