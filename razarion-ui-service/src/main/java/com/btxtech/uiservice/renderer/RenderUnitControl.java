package com.btxtech.uiservice.renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 11.09.2016.
 */
@Deprecated // TODO move to WebGlFacadeConfig
public class RenderUnitControl {
    private static final List<RenderUnitControl> RENDER_UNIT_CONTROLS = new ArrayList<>();
    public static final RenderUnitControl ITEMS = new RenderUnitControl();
    public static final RenderUnitControl NORMAL = new RenderUnitControl();
    public static final RenderUnitControl PARTICLE = new RenderUnitControl().blend(Blend.SOURCE_ALPHA).writeDepthBuffer(false).depthTest(true);
    public static final RenderUnitControl SEMI_TRANSPARENT = new RenderUnitControl().blend(Blend.SOURCE_ALPHA);
    public static final RenderUnitControl SELECTION_FRAME = new RenderUnitControl().depthTest(false).writeDepthBuffer(false);
    public static final RenderUnitControl START_POINT_CIRCLE = new RenderUnitControl().blend(Blend.SOURCE_ALPHA).depthTest(false).writeDepthBuffer(false);
    public static final RenderUnitControl START_POINT_ITEM = new RenderUnitControl().blend(Blend.CONST_ALPHA).constAlpha(0.5);
    public static final RenderUnitControl TERRAIN_ITEM_VISUALIZATION_IMAGE = new RenderUnitControl().depthTest(false).writeDepthBuffer(false);
    public static final RenderUnitControl TERRAIN_ITEM_VISUALIZATION_CORNERS = new RenderUnitControl().depthTest(false).writeDepthBuffer(false);

    public enum Blend {
        SOURCE_ALPHA,
        CONST_ALPHA
    }

    private boolean depthTest = true;
    private boolean writeDepthBuffer = true;
    private Blend blend;
    private double constAlpha;

    public static List<RenderUnitControl> getRenderUnitControls() {
        return RENDER_UNIT_CONTROLS;
    }

    private RenderUnitControl() {
        RENDER_UNIT_CONTROLS.add(this);
    }

    public boolean isDepthTest() {
        return depthTest;
    }

    public boolean isWriteDepthBuffer() {
        return writeDepthBuffer;
    }

    public Blend getBlend() {
        return blend;
    }

    public float getConstAlpha() {
        return (float) constAlpha;
    }

    private RenderUnitControl depthTest(boolean dpDepthTest) {
        this.depthTest = dpDepthTest;
        return this;
    }

    private RenderUnitControl writeDepthBuffer(boolean writeDepthBuffer) {
        this.writeDepthBuffer = writeDepthBuffer;
        return this;
    }

    private RenderUnitControl blend(Blend blend) {
        this.blend = blend;
        return this;
    }

    private RenderUnitControl constAlpha(double constAlpha) {
        this.constAlpha = constAlpha;
        return this;
    }
}
