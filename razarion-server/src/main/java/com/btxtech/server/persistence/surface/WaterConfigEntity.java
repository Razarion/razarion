package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
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
                .reflectionScale(reflectionScale)
                .bumpMapDepth(bumpMapDepth)
                .bumpDistortionScale(bumpDistortionScale)
                .distortionStrength(distortionStrength)
                .distortionDurationSeconds(distortionDurationSeconds);
        if (reflection != null) {
            waterConfig.setReflectionId(reflection.getId());
        }
        if (bumpMap != null) {
            waterConfig.setBumpMapId(bumpMap.getId());
        }
        if (distortion != null) {
            waterConfig.setDistortionId(distortion.getId());
        }
        return waterConfig;
    }

    public void fromWaterConfig(WaterConfig waterConfig, ImagePersistence imagePersistence) {
        internalName = waterConfig.getInternalName();
        waterLevel = waterConfig.getWaterLevel();
        groundLevel = waterConfig.getGroundLevel();
        transparency = waterConfig.getTransparency();
        reflection = imagePersistence.getImageLibraryEntity(waterConfig.getReflectionId());
        reflectionScale = waterConfig.getReflectionScale();
        bumpMap = imagePersistence.getImageLibraryEntity(waterConfig.getBumpMapId());
        bumpMapDepth = waterConfig.getBumpMapDepth();
        bumpDistortionScale = waterConfig.getBumpDistortionScale();
        distortion = imagePersistence.getImageLibraryEntity(waterConfig.getDistortionId());
        distortionStrength = waterConfig.getDistortionStrength();
        distortionDurationSeconds = waterConfig.getDistortionDurationSeconds();
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
