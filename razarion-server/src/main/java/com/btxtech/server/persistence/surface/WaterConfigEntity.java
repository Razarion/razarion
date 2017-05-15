package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.LightConfigEmbeddable;
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
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    private double waterLevel;
    private double waterTransparency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bmId;
    private double bmScale;
    private double bmDepth;
    private double groundLevel;
    private LightConfigEmbeddable lightConfig;

    public WaterConfig toWaterConfig() {
        WaterConfig waterConfig = new WaterConfig();
        waterConfig.setWaterLevel(waterLevel).setGroundLevel(groundLevel).setTransparency(waterTransparency);
        if (bmId != null) {
            waterConfig.setBmId(bmId.getId());
        }
        waterConfig.setBmScale(bmScale).setBmDepth(bmDepth);
        if (lightConfig != null) {
            waterConfig.setLightConfig(lightConfig.toLightConfig());
        }
        return waterConfig;
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
