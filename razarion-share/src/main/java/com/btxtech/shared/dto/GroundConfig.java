package com.btxtech.shared.dto;

import java.util.Objects;

/**
 * Created by Beat
 * 07.05.2016.
 */
public class GroundConfig {
    private int id;
    private String internalName;
    // private PhongMaterialConfig topTexture;
    // private PhongMaterialConfig bottomTexture;
    // private ImageScaleConfig splatting;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public GroundConfig id(int id) {
        setId(id);
        return this;
    }

    public GroundConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    //    public PhongMaterialConfig getTopTexture() {
//        return topTexture;
//    }
//
//    public GroundSkeletonConfig setTopTexture(PhongMaterialConfig topTexture) {
//        this.topTexture = topTexture;
//        return this;
//    }
//
//    public PhongMaterialConfig getBottomTexture() {
//        return bottomTexture;
//    }
//
//    public GroundSkeletonConfig setBottomTexture(PhongMaterialConfig bottomTexture) {
//        this.bottomTexture = bottomTexture;
//        return this;
//    }
//
//    public ImageScaleConfig getSplatting() {
//        return splatting;
//    }
//
//    public GroundSkeletonConfig setSplatting(ImageScaleConfig splatting) {
//        this.splatting = splatting;
//        return this;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroundConfig that = (GroundConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
