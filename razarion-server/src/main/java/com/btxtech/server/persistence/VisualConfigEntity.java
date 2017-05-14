package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.VisualConfig;
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
import java.util.List;

/**
 * Created by Beat
 * 14.05.2017.
 */
@Entity
@Table(name = "VISUAL_CONFIG")
public class VisualConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    private double shadowRotationX;
    private double shadowRotationY;
    private double shadowAlpha;
    private double shape3DLightRotateX;
    private double shape3DLightRotateZ;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity baseItemDemolitionImageId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity buildupTextureId;
    private double waterTransparency;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity waterBmId;
    private double waterBmScale;
    private double waterBmDepth;
    private double waterGroundLevel;
    @AttributeOverrides({
            @AttributeOverride(name = "xRotation", column = @Column(name = "waterLightXRotation")),
            @AttributeOverride(name = "yRotation", column = @Column(name = "waterLightYRotation")),
            @AttributeOverride(name = "diffuse.r", column = @Column(name = "waterLightDiffuseR")),
            @AttributeOverride(name = "diffuse.g", column = @Column(name = "waterLightDiffuseG")),
            @AttributeOverride(name = "diffuse.b", column = @Column(name = "waterLightDiffuseB")),
            @AttributeOverride(name = "diffuse.a", column = @Column(name = "waterLightDiffuseA")),
            @AttributeOverride(name = "ambient.r", column = @Column(name = "waterLightAmbientR")),
            @AttributeOverride(name = "ambient.g", column = @Column(name = "waterLightAmbientG")),
            @AttributeOverride(name = "ambient.b", column = @Column(name = "waterLightAmbientB")),
            @AttributeOverride(name = "ambient.a", column = @Column(name = "waterLightAmbientA")),
            @AttributeOverride(name = "specularIntensity", column = @Column(name = "waterLightSpecularIntensity")),
            @AttributeOverride(name = "specularHardness", column = @Column(name = "waterLightSpecularHardness")),
    })
    private LightConfigEmbeddable waterLightConfig;

    public VisualConfig toVisualConfig() {
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowRotationX(shadowRotationX).setShadowRotationY(shadowRotationY).setShadowAlpha(shadowAlpha);
        visualConfig.setShape3DLightRotateX(shape3DLightRotateX).setShape3DLightRotateZ(shape3DLightRotateZ);
        if(baseItemDemolitionImageId != null) {
            visualConfig.setBaseItemDemolitionImageId(baseItemDemolitionImageId.getId());
        }
        if(buildupTextureId != null) {
            visualConfig.setBuildupTextureId(buildupTextureId.getId());
        }
        WaterConfig waterConfig = new WaterConfig().setTransparency(waterTransparency);
        if(waterBmId != null) {
            waterConfig.setBmId(waterBmId.getId());
        }
        waterConfig.setBmScale(waterBmScale).setBmDepth(waterBmDepth);
        if(waterLightConfig != null) {
            waterConfig.setLightConfig(waterLightConfig.toLightConfig());
        }
        return visualConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VisualConfigEntity that = (VisualConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
