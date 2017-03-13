package com.btxtech.shared.datatypes.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 13.03.2017.
 */
public class WaterUi extends TerrainUi {
    private double groundLevel;
    private Integer bmId;
    private Double bmScale;
    private double bmDepth;
    private double transparency;
    private LightConfig lightConfig;
    private Rectangle2D aabb;

    public WaterUi(VisualConfig visualConfig) {
        groundLevel = visualConfig.getWaterGroundLevel();
        bmId = visualConfig.getWaterBmId();
        bmDepth = visualConfig.getWaterBmDepth();
        bmScale = visualConfig.getWaterBmScale();
        transparency = visualConfig.getWaterTransparency();
        lightConfig = visualConfig.getWaterLightConfig();
    }

    public WaterUi(int elementCount, Float32ArrayEmu vertices, Float32ArrayEmu norms, Float32ArrayEmu tangents, Rectangle2D aabb) {
        super(elementCount, vertices, norms, tangents);
        this.aabb = aabb;
    }

    public boolean isValid() {
        return getElementCount() > 0;
    }

    public Rectangle2D getAabb() {
        return aabb;
    }

    public double getGroundLevel() {
        return groundLevel;
    }

    public Integer getBmId() {
        return bmId;
    }

    public Double getBmScale() {
        return bmScale;
    }

    public double getTransparency() {
        return transparency;
    }

    public double getBmDepth() {
        return bmDepth;
    }

    public double getWaterAnimation() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 0);
    }

    public double getWaterAnimation2() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 500);
    }

    private double getWaterAnimation(long millis, int durationMs, int offsetMs) {
        return Math.sin(((millis % durationMs) / (double) durationMs + ((double) offsetMs / (double) durationMs)) * MathHelper.ONE_RADIANT);
    }

    public LightConfig getLightConfig() {
        return lightConfig;
    }

    public void setLightConfig(LightConfig lightConfig) {
        this.lightConfig = lightConfig;
    }

    public void setBuffers(WaterUi waterUi) {
        super.setBuffers(waterUi);
        aabb = waterUi.getAabb();
    }
}
