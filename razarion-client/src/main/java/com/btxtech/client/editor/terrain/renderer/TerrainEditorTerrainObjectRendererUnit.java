package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.TerrainEditorService;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.05.2016.
 */
@ColorBufferRenderer
@Dependent
public class TerrainEditorTerrainObjectRendererUnit extends AbstractRenderUnit<Void> {
    private static final int TRIANGLE_COUNT = 10;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private TerrainEditorService terrainEditor;
    private VertexShaderAttribute vertices;
    private WebGLUniformLocation modelMatrix;
    private WebGLUniformLocation uDelete;
    private WebGLUniformLocation uHover;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(Shaders.INSTANCE.terrainObjectEditorVertexShader(), Shaders.INSTANCE.terrainObjectEditorFragmentShader()).enableTransformation(false));
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        modelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_NORM_MATRIX);
        uDelete = webGlFacade.getUniformLocation("uDelete");
        uHover = webGlFacade.getUniformLocation("uHover");
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void fillBuffers(Void ignore) {
        List<Vertex> triangleFan = new ArrayList<>();
        triangleFan.add(new Vertex(0, 0, 0));
        for (int i = 0; i < TRIANGLE_COUNT; i++) {
            double angle = MathHelper.ONE_RADIANT * (double) i / (double) TRIANGLE_COUNT;
            triangleFan.add(new Vertex(DecimalPosition.createVector(angle, 1), 0));
        }
        triangleFan.add(new Vertex(1, 0, 0));
        vertices.fillBuffer(triangleFan);
        setElementCount(triangleFan.size());
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        webGlFacade.uniform1b(uDelete, terrainEditor.isDeletePressed());
    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(modelMatrix, modelMatrices.getModel());
        webGlFacade.uniform1b(uHover, modelMatrices.getProgress() > 0.0);

        vertices.activate();

        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLE_FAN);
    }
}
