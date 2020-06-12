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
    private double waterLevel;
    private double waterTransparency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity normMapId; // TODO rename on db
    private double groundLevel;

    public Integer getId() {
        return id;
    }

    public WaterConfig toWaterConfig() {
        WaterConfig waterConfig = new WaterConfig();
        // TODO waterConfig.setWaterLevel(waterLevel).setGroundLevel(groundLevel).setTransparency(waterTransparency);
        if (normMapId != null) {
            waterConfig.setNormMapId(normMapId.getId());
        }
        // TODO ----------
        waterConfig.setReflectionId(96); // TODO
        waterConfig.setReflectionScale(100); // TODO
        waterConfig.setDistortionId(89); // TODO
        waterConfig.setDistortionScale(16); // TODO
        waterConfig.setDistortionStrength(0.11); // TODO
        waterConfig.setDistortionDurationSeconds(20); // TODO
        waterConfig.setNormMapDepth(1.0);
        // TODO ends -----

        return waterConfig;
    }

    public void fromWaterConfig(WaterConfig waterConfig, ImagePersistence imagePersistence) {
        waterLevel = waterConfig.getWaterLevel();
        groundLevel = waterConfig.getGroundLevel();
        waterTransparency = waterConfig.getTransparency();
        normMapId = imagePersistence.getImageLibraryEntity(waterConfig.getNormMapId());
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
