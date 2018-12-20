package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.dto.SpecularLightConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * Created by Beat
 * 28.05.2016.
 */
@Embeddable
public class SpecularLightConfigEmbeddable {
    private Double specularIntensity;
    private Double specularHardness;

    public SpecularLightConfig toLightConfig() {
        SpecularLightConfig specularLightConfig = new SpecularLightConfig();
        specularLightConfig.setSpecularIntensity(specularIntensity);
        specularLightConfig.setSpecularHardness(specularHardness);
        return specularLightConfig;
    }

    public void fromLightConfig(SpecularLightConfig specularLightConfig) {
        specularIntensity = specularLightConfig.getSpecularIntensity();
        specularHardness = specularLightConfig.getSpecularHardness();
    }

    @Override
    public String toString() {
        return "SpecularLightConfigEmbeddable{" +
                ", specularIntensity=" + specularIntensity +
                ", specularHardness=" + specularHardness +
                '}';
    }
}
