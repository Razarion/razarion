package com.btxtech.server.persistence;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.object.TerrainObjectPositionEntity;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.TerrainSlopePositionEntity;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @JoinColumn(name = "planet")
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
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "lightDirectionX")),
            @AttributeOverride(name = "y", column = @Column(name = "lightDirectionY")),
            @AttributeOverride(name = "z", column = @Column(name = "lightDirectionZ")),
    })
    private Vertex lightDirection;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "ambientR")),
            @AttributeOverride(name = "g", column = @Column(name = "ambientG")),
            @AttributeOverride(name = "b", column = @Column(name = "ambientB")),
            @AttributeOverride(name = "a", column = @Column(name = "ambientA")),
    })
    private Color ambient;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "diffuseR")),
            @AttributeOverride(name = "g", column = @Column(name = "diffuseG")),
            @AttributeOverride(name = "b", column = @Column(name = "diffuseB")),
            @AttributeOverride(name = "a", column = @Column(name = "diffuseA")),
    })
    private Color diffuse;
    private double shadowAlpha;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] miniMapImage;

    public Integer getId() {
        return id;
    }

    public PlanetConfig toPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig().id(id).internalName(internalName);
        if(groundConfig != null) {
            planetConfig.setGroundConfigId(groundConfig.getId());
        }
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

    public void fromPlanetConfig(PlanetConfig planetConfig, GroundCrudPersistence groundCrudPersistence, ItemTypePersistence itemTypePersistence) {
        groundConfig = groundCrudPersistence.getEntity(planetConfig.getGroundConfigId());
        houseSpace = planetConfig.getHouseSpace();
        startRazarion = planetConfig.getStartRazarion();
        startBaseItemType = itemTypePersistence.readBaseItemTypeEntity(planetConfig.getStartBaseItemTypeId());
        if (itemTypeLimitation == null) {
            itemTypeLimitation = new HashMap<>();
        }
        itemTypeLimitation.clear();
        for (Map.Entry<Integer, Integer> entry : planetConfig.getItemTypeLimitation().entrySet()) {
            itemTypeLimitation.put(itemTypePersistence.readBaseItemTypeEntity(entry.getKey()), entry.getValue());
        }
    }

    public PlanetVisualConfig toPlanetVisualConfig() {
        PlanetVisualConfig planetVisualConfig = new PlanetVisualConfig();
        planetVisualConfig.setShadowAlpha(shadowAlpha);
        planetVisualConfig.setLightDirection(Optional.ofNullable(lightDirection).orElse(Vertex.Z_NORM_NEG));
        planetVisualConfig.setAmbient(Optional.ofNullable(ambient).orElse(Color.GREY));
        planetVisualConfig.setDiffuse(Optional.ofNullable(diffuse).orElse(Color.GREY));
        return planetVisualConfig;
    }

    public void fromPlanetVisualConfig(PlanetVisualConfig planetVisualConfig) {
        lightDirection = planetVisualConfig.getLightDirection();
        shadowAlpha = planetVisualConfig.getShadowAlpha();
        ambient = planetVisualConfig.getAmbient();
        diffuse = planetVisualConfig.getDiffuse();
    }

    public void setGroundConfig(GroundConfigEntity groundConfig) {
        this.groundConfig = groundConfig;
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
