package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.dto.LightConfig;

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
public class LightConfigEmbeddable {
    private Double xRotation;
    private Double yRotation;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "diffuseR")),
            @AttributeOverride(name = "g", column = @Column(name = "diffuseG")),
            @AttributeOverride(name = "b", column = @Column(name = "diffuseB")),
            @AttributeOverride(name = "a", column = @Column(name = "diffuseA")),
    })
    @Embedded
    private Color diffuse;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "ambientR")),
            @AttributeOverride(name = "g", column = @Column(name = "ambientG")),
            @AttributeOverride(name = "b", column = @Column(name = "ambientB")),
            @AttributeOverride(name = "a", column = @Column(name = "ambientA")),
    })
    @Embedded
    private Color ambient;
    private Double specularIntensity;
    private Double specularHardness;

    public LightConfig toLightConfig() {
        LightConfig lightConfig = new LightConfig();
        lightConfig.setRotationX(xRotation);
        lightConfig.setRotationY(yRotation);
        lightConfig.setAmbient(ambient);
        lightConfig.setDiffuse(diffuse);
        lightConfig.setSpecularIntensity(specularIntensity);
        lightConfig.setSpecularHardness(specularHardness);
        return lightConfig;
    }

    public void fromLightConfig(LightConfig lightConfig) {
        xRotation = lightConfig.getRotationX();
        yRotation = lightConfig.getRotationY();
        ambient = lightConfig.getAmbient();
        diffuse = lightConfig.getDiffuse();
        specularIntensity = lightConfig.getSpecularIntensity();
        specularHardness = lightConfig.getSpecularHardness();
    }

    @Override
    public String toString() {
        return "LightConfigEmbeddable{" +
                "xRotation=" + Math.toDegrees(xRotation) +
                ", yRotation=" + Math.toDegrees(yRotation) +
                ", diffuse=" + diffuse +
                ", ambient=" + ambient +
                ", specularIntensity=" + specularIntensity +
                ", specularHardness=" + specularHardness +
                '}';
    }
}
