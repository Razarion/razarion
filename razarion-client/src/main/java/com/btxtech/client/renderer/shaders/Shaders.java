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

    // ---------------------- OLD ----------------------
    @Source("Monitor.vert")
    TextResource monitorVertexShader();

    @Source("Monitor.frag")
    TextResource monitorFragmentShader();

    @Source("DepthBufferVP.vert")
    TextResource depthBufferVPVertexShader();

    @Source("DebugVector.vert")
    TextResource debugVectorVertexShader();

    @Source("DebugVector.frag")
    TextResource debugVectorFragmentShader();

    @Source("TerrainEditor.vert")
    TextResource terrainEditorVertexShader();

    @Source("TerrainEditor.frag")
    TextResource terrainEditorFragmentShader();

    @Source("TerrainEditorCursor.vert")
    TextResource terrainEditorCursorVertexShader();

    @Source("TerrainEditorCursor.frag")
    TextResource terrainEditorCursorFragmentShader();

    @Source("TerrainObjectEditor.vert")
    TextResource terrainObjectEditorVertexShader();

    @Source("TerrainObjectEditor.frag")
    TextResource terrainObjectEditorFragmentShader();

    @Source("RgbaMvp.vert")
    @Deprecated
    TextResource rgbaMvpVertexShader();

    @Source("Rgba.frag")
    @Deprecated
    TextResource rgbaFragmentShader();

    @Source("RgbaVp.vert")
    @Deprecated
    TextResource rgbaVpVertexShader();

    @Source("Particle.vert")
    TextResource particleVertexShader();

    @Source("Particle.frag")
    TextResource particleFragmentShader();

    @Source("ParticleDeptBuffer.frag")
    TextResource particleDeptBufferFragmentShader();
}
