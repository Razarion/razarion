package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.dto.WaterConfig;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 14.05.2017.
 */
@Entity
@Table(name = "WATER_CONFIG")
public class WaterConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private double waterLevel;
    private double groundLevel;
    private double transparency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity reflection;
    private double reflectionScale;
    private double fresnelOffset;
    private double fresnelDelta;
    private double shininess;
    private double specularStrength;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bumpMap;
    private double bumpMapDepth;
    private double bumpDistortionScale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity distortion;
    private double distortionStrength;
    private double distortionDurationSeconds;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity material;


    public Integer getId() {
        return id;
    }

    public WaterConfig toWaterConfig() {
        WaterConfig waterConfig = new WaterConfig()
                .id(id)
                .internalName(internalName)
                .waterLevel(waterLevel)
                .groundLevel(groundLevel)
                .transparency(transparency)
                .shininess(shininess)
                .specularStrength(specularStrength)
                .normalMapDepth(bumpMapDepth)
                .distortionStrength(distortionStrength)
                .distortionAnimationSeconds(distortionDurationSeconds)
                .material(PersistenceUtil.extractId(material, ThreeJsModelConfigEntity::getId));
        if (reflection != null) {
            waterConfig.setReflectionId(reflection.getId());
        }
        if (bumpMap != null) {
            waterConfig.setNormalMapId(bumpMap.getId());
        }
        if (distortion != null) {
            waterConfig.setDistortionId(distortion.getId());
        }
        return waterConfig;
    }

    public void fromWaterConfig(WaterConfig waterConfig, ImagePersistence imagePersistence, ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        internalName = waterConfig.getInternalName();
        waterLevel = waterConfig.getWaterLevel();
        groundLevel = waterConfig.getGroundLevel();
        transparency = waterConfig.getTransparency();
        reflection = imagePersistence.getImageLibraryEntity(waterConfig.getReflectionId());
        shininess = waterConfig.getShininess();
        specularStrength = waterConfig.getSpecularStrength();
        bumpMap = imagePersistence.getImageLibraryEntity(waterConfig.getNormalMapId());
        bumpMapDepth = waterConfig.getNormalMapDepth();
        distortion = imagePersistence.getImageLibraryEntity(waterConfig.getDistortionId());
        distortionStrength = waterConfig.getDistortionStrength();
        distortionDurationSeconds = waterConfig.getDistortionAnimationSeconds();
        material = threeJsModelCrudPersistence.getEntity(waterConfig.getMaterial());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WaterConfigEntity that = (WaterConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
