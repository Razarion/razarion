package com.btxtech.uiservice.renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 11.09.2016.
 */
public class RenderUnitControl {
    private static final List<RenderUnitControl> RENDER_UNIT_CONTROLS = new ArrayList<>();
    public static final RenderUnitControl TERRAIN = new RenderUnitControl();
    public static final RenderUnitControl SELECTED_ITEM = new RenderUnitControl().setBlend(Blend.SOURCE_ALPHA).setWriteDepthBuffer(false).setDpDepthTest(false);
    public static final RenderUnitControl ITEMS = new RenderUnitControl();
    public static final RenderUnitControl NORMAL = new RenderUnitControl();
    public static final RenderUnitControl WATER = new RenderUnitControl().setBlend(Blend.SOURCE_ALPHA);
    public static final RenderUnitControl PARTICLE = new RenderUnitControl().setBlend(Blend.SOURCE_ALPHA).setWriteDepthBuffer(false).setDpDepthTest(true);
    public static final RenderUnitControl SEMI_TRANSPARENT = new RenderUnitControl().setBlend(Blend.SOURCE_ALPHA);
    public static final RenderUnitControl STATUS_BAR = new RenderUnitControl().setBlend(Blend.SOURCE_ALPHA).setWriteDepthBuffer(false).setDpDepthTest(false);
    public static final RenderUnitControl SELECTION_FRAME = new RenderUnitControl().setDpDepthTest(false).setWriteDepthBuffer(false);
    public static final RenderUnitControl START_POINT_CIRCLE = new RenderUnitControl().setBlend(Blend.SOURCE_ALPHA).setDpDepthTest(false).setWriteDepthBuffer(false);
    public static final RenderUnitControl START_POINT_ITEM = new RenderUnitControl().setBlend(Blend.CONST_ALPHA).setConstAlpha(0.5).setBackCull(true);
    public static final RenderUnitControl TERRAIN_TIP_IMAGE = new RenderUnitControl().setDpDepthTest(false).setWriteDepthBuffer(false);
    public static final RenderUnitControl TERRAIN_TIP_CORNERS = new RenderUnitControl().setDpDepthTest(false).setWriteDepthBuffer(false);

    public enum Blend {
        SOURCE_ALPHA,
        CONST_ALPHA
    }

    private boolean dpDepthTest = true;
    private boolean writeDepthBuffer = true;
    private boolean backCull;
    private Blend blend;
    private double constAlpha;

    public static List<RenderUnitControl> getRenderUnitControls() {
        return RENDER_UNIT_CONTROLS;
    }

    private RenderUnitControl() {
        RENDER_UNIT_CONTROLS.add(this);
    }

    public boolean isDpDepthTest() {
        return dpDepthTest;
    }

    private RenderUnitControl setDpDepthTest(boolean dpDepthTest) {
        this.dpDepthTest = dpDepthTest;
        return this;
    }

    public boolean isWriteDepthBuffer() {
        return writeDepthBuffer;
    }

    private RenderUnitControl setWriteDepthBuffer(boolean writeDepthBuffer) {
        this.writeDepthBuffer = writeDepthBuffer;
        return this;
    }

    public boolean isBackCull() {
        return backCull;
    }

    private RenderUnitControl setBackCull(boolean backCull) {
        this.backCull = backCull;
        return this;
    }

    public Blend getBlend() {
        return blend;
    }

    private RenderUnitControl setBlend(Blend blend) {
        this.blend = blend;
        return this;
    }

    public float getConstAlpha() {
        return (float) constAlpha;
    }

    private RenderUnitControl setConstAlpha(double constAlpha) {
        this.constAlpha = constAlpha;
        return this;
    }
}
