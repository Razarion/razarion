package com.btxtech.client.renderer.shaders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Created by  Beat
 * 04.04.2015.
 */
public interface Shaders extends ClientBundle {
    Shaders INSTANCE = GWT.create(Shaders.class);

    @Source("Skeleton.vert")
    TextResource skeletonVertexShader();

    @Source("Skeleton.frag")
    TextResource skeletonFragmentShader();

    @Source("ViewPerspectiveWire.vert")
    TextResource viewPerspectiveWireVertexShader();

    @Source("ViewPerspectiveWire.frag")
    TextResource viewPerspectiveWireFragmentShader();

    @Source("ModelViewPerspectiveWire.vert")
    TextResource modelViewPerspectiveWireVertexShader();

    @Source("ModelViewPerspectiveWire.frag")
    TextResource modelViewPerspectiveWireFragmentShader();

    @Source("Monitor.vert")
    TextResource monitorVertexShader();

    @Source("Monitor.frag")
    TextResource monitorFragmentShader();

    @Source("DepthBufferVP.vert")
    TextResource depthBufferVPVertexShader();

    @Source("DepthBufferVP.frag")
    TextResource depthBufferVPFragmentShader();

    @Source("DebugVector.vert")
    TextResource debugVectorVertexShader();

    @Source("DebugVector.frag")
    TextResource debugVectorFragmentShader();

    @Source("WaterCustom.glsl")
    TextResource customWater();

    @Source("VertexContainerCustom.frag")
    TextResource vertexContainerCustomShader();

    @Source("BuildupVertexContainer.vert")
    TextResource buildupVertexContainerVertexShader();

    @Source("BuildupVertexContainer.frag")
    TextResource buildupVertexContainerFragmentShader();

    @Source("BuildupVertexContainerDepthBuffer.vert")
    TextResource buildupVertexContainerDeptBufferVertexShader();

    @Source("BuildupVertexContainerDepthBuffer.frag")
    TextResource buildupVertexContainerDeptBufferFragmentShader();

    @Source("DemolitionVertexContainer.vert")
    TextResource demolitionVertexContainerVertexShader();

    @Source("DemolitionVertexContainer.frag")
    TextResource demolitionVertexContainerFragmentShader();

    @Source("GroundCustom.glsl")
    TextResource groundCustom();

    @Source("SlopeCustom.glsl")
    TextResource slopeCustom();

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
    TextResource rgbaMvpVertexShader();

    @Source("Rgba.frag")
    TextResource rgbaFragmentShader();

    @Source("RgbaVp.vert")
    TextResource rgbaVpVertexShader();

    @Source("Particle.vert")
    TextResource particleVertexShader();

    @Source("Particle.frag")
    TextResource particleFragmentShader();

    @Source("ParticleDeptBuffer.frag")
    TextResource particleDeptBufferFragmentShader();

    @Source("CommonVisibility.vert")
    TextResource commonVisibilityVertexShader();

    @Source("ItemMarker.frag")
    TextResource itemMarkerFragmentShader();

    @Source("StatusBar.frag")
    TextResource statusBarFragmentShader();
}
