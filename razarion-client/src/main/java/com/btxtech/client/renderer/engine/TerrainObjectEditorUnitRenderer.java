package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.terrain.TerrainObjectEditorSelectedEvent;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
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
public class TerrainObjectEditorUnitRenderer extends AbstractWebGlUnitRenderer {
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private VertexShaderAttribute vertices;
    private Matrix4 modelPosition = Matrix4.createIdentity();
    private int cursoType;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.terrainObjectEditorVertexShader(), Shaders.INSTANCE.terrainObjectEditorFragmentShader());
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
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
        setElementCount(triangleFan.size());
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        useProgram();

        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        uniformMatrix4fv(U_VIEW_MATRIX, camera.getMatrix());
        uniformMatrix4fv(U_MODEL_MATRIX, modelPosition);

        uniform1i(U_CURSOR_TYPE, cursoType);

        vertices.activate();

        // Draw
        drawArrays(WebGLRenderingContext.TRIANGLE_FAN);
    }
}
