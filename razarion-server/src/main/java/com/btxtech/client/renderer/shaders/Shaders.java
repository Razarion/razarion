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

    @Source("Ground.vert")
    TextResource groundVertexShader();

    @Source("Ground.frag")
    TextResource groundFragmentShader();

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

    @Source("Slope.vert")
    TextResource slopeVertexShader();

    @Source("Slope.frag")
    TextResource slopeFragmentShader();

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

    @Source("TextureTerrainObject.vert")
    TextResource textureTerrainObjectVertexShader();

    @Source("TextureTerrainObject.frag")
    TextResource textureTerrainObjectFragmentShader();

    @Source("TextureTerrainObjectDepth.vert")
    TextResource textureTerrainObjectDepthVertexShader();

    @Source("TextureTerrainObjectDepth.frag")
    TextResource textureTerrainObjectDepthFragmentShader();
}
