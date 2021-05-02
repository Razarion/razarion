package com.btxtech.client.renderer.shaders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Created by  Beat
 * 04.04.2015.
 */
public interface Shaders extends ClientBundle {
    Shaders SHADERS = GWT.create(Shaders.class);

    @Source("Skeleton.vert")
    TextResource skeletonVertexShader();

    @Source("Skeleton.frag")
    TextResource skeletonFragmentShader();

    @Source("GroundCustom.glsl")
    TextResource groundCustom();

    @Source("WaterCustom.glsl")
    TextResource customWater();

    @Source("VertexContainerCustom.glsl")
    TextResource vertexContainerCustomShader();

    @Source("SlopeCustom.glsl")
    TextResource slopeCustom();

    @Source("RgbaCustom.glsl")
    TextResource customRgba();

    @Source("ItemMarkerCustom.glsl")
    TextResource itemMarkerCustom();

    @Source("StatusBarCustom.glsl")
    TextResource statusBarCustom();

    @Source("ParticleCustom.glsl")
    TextResource particleCustom();

    @Source("TerrainEditorCursorCustom.glsl")
    TextResource terrainEditorCursorCustom();

    @Source("TerrainObjectEditorCustom.glsl")
    TextResource terrainObjectEditorCustom();

    @Source("TerrainEditorSlopeCustom.glsl")
    TextResource terrainEditorSlopeCustom();

    @Source("MonitorCustom.glsl")
    TextResource monitorCustom();

    // ---------------------- OLD ----------------------

    @Source("RgbaMvp.vert")
    @Deprecated
    TextResource rgbaMvpVertexShader();

    @Source("Rgba.frag")
    @Deprecated
    TextResource rgbaFragmentShader();
}
