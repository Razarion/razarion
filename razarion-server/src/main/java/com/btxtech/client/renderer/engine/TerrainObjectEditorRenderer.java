package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.object.TerrainObjectEditorSelectedEvent;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
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
 * 13.05.2016.
 */
@Dependent
public class TerrainObjectEditorRenderer extends AbstractRenderer {
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private int elementCount;
    private VertexShaderAttribute vertices;
    private Matrix4 modelPosition = Matrix4.createIdentity();
    private int cursoType;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainObjectEditorVertexShader(), Shaders.INSTANCE.terrainObjectEditorFragmentShader());
        vertices = createVertexShaderAttribute("aVertexPosition");
    }

    public void onTerrainEditorCursorShapeEvent(@Observes TerrainObjectEditorSelectedEvent terrainObjectEditorSelectedEvent) {
        cursoType = terrainObjectEditorSelectedEvent.getCursorType().ordinal();
        Vertex position = terrainObjectEditorSelectedEvent.getPosition();
        if (position != null) {
            modelPosition = Matrix4.createTranslation(position.getX(), position.getY(), position.getZ());
        }
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void fillBuffers() {
        List<Vertex> triangleFan = new ArrayList<>();
        triangleFan.add(new Vertex(0, 0, 0));
        int count = 10;
        for (int i = 0; i < count; i++) {
            double angle = MathHelper.ONE_RADIANT * (double) i / (double) count;
            triangleFan.add(new Vertex(DecimalPosition.createVector(angle, 10), 0));
        }
        triangleFan.add(new Vertex(10, 0, 0));
        vertices.fillBuffer(triangleFan);
        elementCount = triangleFan.size();
    }

    @Override
    public void draw() {
        useProgram();

        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());
        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uMMatrix", modelPosition);

        uniform1i("uCursorType", cursoType);

        vertices.activate();

        // Draw
        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLE_FAN, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
