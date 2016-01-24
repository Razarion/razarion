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

    @Source("ViewPerspectiveWire.vert")
    TextResource viewPerspectiveWireVertexShader();

    @Source("ViewPerspectiveWire.frag")
    TextResource viewPerspectiveWireFragmentShader();

    @Source("ModelViewPerspectiveWire.vert")
    TextResource modelViewPerspectiveWireVertexShader();

    @Source("ModelViewPerspectiveWire.frag")
    TextResource modelViewPerspectiveWireFragmentShader();

    @Source("TerrainSurface.vert")
    TextResource terrainSurfaceVertexShader();

    @Source("TerrainSurface.frag")
    TextResource terrainSurfaceFragmentShader();

    @Source("TerrainObject.vert")
    TextResource terrainObjectVertexShader();

    @Source("TerrainObject.frag")
    TextResource terrainObjectFragmentShader();

    @Source("Monitor.vert")
    TextResource monitorVertexShader();

    @Source("Monitor.frag")
    TextResource monitorFragmentShader();

    @Source("DepthBuffer.vert")
    TextResource depthBufferVertexShader();

    @Source("DepthBuffer.frag")
    TextResource depthBufferFragmentShader();

    @Source("DebugVector.vert")
    TextResource debugVectorVertexShader();

    @Source("DebugVector.frag")
    TextResource debugVectorFragmentShader();

    @Source("Water.vert")
    TextResource waterVertexShader();

    @Source("Water.frag")
    TextResource waterFragmentShader();

    @Source("Unit.vert")
    TextResource unitVertexShader();

    @Source("Unit.frag")
    TextResource unitFragmentShader();
}
