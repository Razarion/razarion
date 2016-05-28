package com.btxtech.server.terrain;

import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.primitives.Color;

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
    double xRotation;
    double yRotation;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "diffuseR", nullable = false)),
            @AttributeOverride(name = "g", column = @Column(name = "diffuseG", nullable = false)),
            @AttributeOverride(name = "b", column = @Column(name = "diffuseB", nullable = false)),
            @AttributeOverride(name = "a", column = @Column(name = "diffuseA", nullable = false)),
    })
    @Embedded
    private Color diffuse;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "ambientR", nullable = false)),
            @AttributeOverride(name = "g", column = @Column(name = "ambientG", nullable = false)),
            @AttributeOverride(name = "b", column = @Column(name = "ambientB", nullable = false)),
            @AttributeOverride(name = "a", column = @Column(name = "ambientA", nullable = false)),
    })
    @Embedded
    private Color ambient;
    private double specularIntensity;
    private double specularHardness;

    public LightConfig toLightConfig() {
        LightConfig lightConfig = new LightConfig();
        lightConfig.setXRotation(xRotation);
        lightConfig.setYRotation(yRotation);
        lightConfig.setAmbient(ambient);
        lightConfig.setDiffuse(diffuse);
        lightConfig.setSpecularIntensity(specularIntensity);
        lightConfig.setSpecularHardness(specularHardness);
        return lightConfig;
    }

    public void fromLightConfig(LightConfig lightConfig) {
        xRotation = lightConfig.getXRotation();
        yRotation = lightConfig.getYRotation();
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
