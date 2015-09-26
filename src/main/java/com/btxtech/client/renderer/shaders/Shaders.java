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

    @Source("TerrainSurfaceVertexShader.shd")
    TextResource terrainSurfaceVertexShader();

    @Source("TerrainSurfaceFragmentShader.shd")
    TextResource terrainSurfaceFragmentShader();

    @Source("TerrainSurfaceWireVertexShader.shd")
    TextResource terrainSurfaceWireVertexShader();

    @Source("TerrainSurfaceWireFragmentShader.shd")
    TextResource terrainSurfaceWireFragmentShader();

    @Source("TerrainObjectVertexShader.shd")
    TextResource terrainObjectVertexShader();

    @Source("TerrainObjectFragmentShader.shd")
    TextResource terrainObjectFragmentShader();

    @Source("TerrainObjectWireVertexShader.shd")
    TextResource terrainObjectWireVertexShader();

    @Source("TerrainObjectWireFragmentShader.shd")
    TextResource terrainObjectWireFragmentShader();

    @Source("MonitorVertexShader.shd")
    TextResource monitorVertexShader();

    @Source("MonitorFragmentShader.shd")
    TextResource monitorFragmentShader();

    @Source("DebugVertexShader.shd")
    TextResource debugVertexShader();

    @Source("DebugFragmentShader.shd")
    TextResource debugFragmentShader();

    @Source("DepthBufferVertexShader.shd")
    TextResource depthBufferVertexShader();

    @Source("DepthBufferFragmentShader.shd")
    TextResource depthBufferFragmentShader();

}
