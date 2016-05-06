package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.client.editor.terrain.TerrainEditorSlopeSelectedEvent;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Polygon2D;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 04.05.2016.
 */
@Dependent
public class TerrainEditorRenderer extends AbstractRenderer {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private TerrainEditor terrainEditor;
    private VertexShaderAttribute vertices;
    private int elementCount;
    private boolean selected;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainEditorVertexShader(), Shaders.INSTANCE.terrainEditorFragmentShader());
        vertices = createVertexShaderAttribute("aVertexPosition");
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void fillBuffers() {
        Polygon2D polygon2D = terrainEditor.getSlopePolygon(getId());
        List<Vertex> corners = new ArrayList<>();
        for (DecimalPosition position : polygon2D.getCorners()) {
            corners.add(new Vertex(position.getX(), position.getY(), 0));
        }
        vertices.fillBuffer(corners);
        elementCount = corners.size();
    }

    public void onTerrainEditorSlopeSelectedEvent(@Observes TerrainEditorSlopeSelectedEvent terrainEditorSlopeSelectedEvent) {
        selected = getId() == terrainEditorSlopeSelectedEvent.getSlopeId();
    }

    @Override
    public void draw() {
        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());

        uniform1b("uSelected", selected);

        vertices.activate();

        // Draw
        getCtx3d().drawArrays(WebGLRenderingContext.LINE_LOOP, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
