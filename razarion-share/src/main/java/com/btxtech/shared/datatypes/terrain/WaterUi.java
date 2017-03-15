package com.btxtech.shared.datatypes.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 13.03.2017.
 */
public class WaterUi extends TerrainUi {
    private WaterConfig waterConfig;
    private Rectangle2D aabb;

    public WaterUi(WaterConfig waterConfig) {
        this.waterConfig = waterConfig;
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
        return waterConfig.getGroundLevel();
    }

    public Integer getBmId() {
        return waterConfig.getBmId();
    }

    public Double getBmScale() {
        return waterConfig.getBmScale();
    }

    public double getTransparency() {
        return waterConfig.getTransparency();
    }

    public double getBmDepth() {
        return waterConfig.getBmDepth();
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
        return waterConfig.getLightConfig();
    }

    public void setBuffers(WaterUi waterUi) {
        super.setBuffers(waterUi);
        aabb = waterUi.getAabb();
    }

    public void setWaterConfig(WaterConfig waterConfig) {
        this.waterConfig = waterConfig;
    }
}
