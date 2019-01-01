package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.SpecularLightConfigEmbeddable;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.dto.WaterConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Optional;

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
    private ImageLibraryEntity bmId;
    private double bmScale;
    private double bmDepth;
    private double groundLevel;
    private SpecularLightConfigEmbeddable specularLightConfig;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "colorRValue")),
            @AttributeOverride(name = "g", column = @Column(name = "colorGValue")),
            @AttributeOverride(name = "b", column = @Column(name = "colorBValue")),
            @AttributeOverride(name = "a", column = @Column(name = "colorAValue")),
    })
    private Color color;

    public WaterConfig toWaterConfig() {
        WaterConfig waterConfig = new WaterConfig();
        waterConfig.setWaterLevel(waterLevel).setGroundLevel(groundLevel).setColor(Optional.ofNullable(color).orElse(new Color(0, 0, 1))).setTransparency(waterTransparency);
        if (bmId != null) {
            waterConfig.setBmId(bmId.getId());
        }
        waterConfig.setBmScale(bmScale).setBmDepth(bmDepth);
        if (specularLightConfig != null) {
            waterConfig.setSpecularLightConfig(specularLightConfig.toLightConfig());
        }
        return waterConfig;
    }

    public void fromWaterConfig(WaterConfig waterConfig, ImagePersistence imagePersistence) {
        waterLevel = waterConfig.getWaterLevel();
        groundLevel = waterConfig.getGroundLevel();
        color = waterConfig.getColor();
        waterTransparency = waterConfig.getTransparency();
        bmId = imagePersistence.getImageLibraryEntity(waterConfig.getBmId());
        bmScale = waterConfig.getBmScale();
        bmDepth = waterConfig.getBmDepth();
        specularLightConfig.fromLightConfig(waterConfig.getSpecularLightConfig());
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
