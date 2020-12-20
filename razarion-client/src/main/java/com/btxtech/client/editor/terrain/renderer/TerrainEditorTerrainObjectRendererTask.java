package com.btxtech.client.editor.terrain.renderer;

import com.btxtech.client.editor.terrain.TerrainEditorService;
import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.subtask.AbstractWebGlRenderTask;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;
import elemental2.webgl.WebGLRenderingContext;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.05.2016.
 */
@Dependent
public class TerrainEditorTerrainObjectRendererTask extends AbstractWebGlRenderTask<Void> {
    private static final int TRIANGLE_COUNT = 10;
    @Inject
    private TerrainEditorService terrainEditor;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(Void ignore) {
        return new WebGlFacadeConfig(Shaders.SHADERS.terrainObjectEditorCustom())
                .enableTransformation(false)
                .blend(WebGlFacadeConfig.Blend.SOURCE_ALPHA)
                .depthTest(false)
                .writeDepthBuffer(false)
                .drawMode(WebGLRenderingContext.TRIANGLE_FAN);
    }

    @Override
    protected void setup(Void ignore) {
        List<Vertex> triangleFan = new ArrayList<>();
        triangleFan.add(new Vertex(0, 0, 0));
        for (int i = 0; i < TRIANGLE_COUNT; i++) {
            double angle = MathHelper.ONE_RADIANT * (double) i / (double) TRIANGLE_COUNT;
            triangleFan.add(new Vertex(DecimalPosition.createVector(angle, 1), 0));
        }
        triangleFan.add(new Vertex(1, 0, 0));
        setupVec3VertexPositionArray(triangleFan);

        setupUniform("uDelete", UniformLocation.Type.B, () -> terrainEditor.isDeletePressed());
        setupModelMatrixUniform("uHover", UniformLocation.Type.B, (modelMatrices) -> modelMatrices.getProgress() > 0.0);
    }
}
