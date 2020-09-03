package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.visualization.AbstractInGameItemCornerRendererUnit;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 07.12.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientInGameItemCornerRendererUnit extends AbstractInGameItemCornerRendererUnit {
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private WebGLUniformLocation colorUniformLocation;
    private WebGLUniformLocation modelMatrix;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(Shaders.INSTANCE.rgbaMvpVertexShader(), Shaders.INSTANCE.rgbaVpFragmentShader()).enableTransformation(false));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        colorUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_COLOR);
        modelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_MATRIX);
    }

    @Override
    protected void fillBuffers(List<Vertex> vertices) {
        positions.fillBuffer(vertices);
    }

    @Override
    protected void prepareDraw(Color cornerColor) {
        webGlFacade.useProgram();

        webGlFacade.uniform4f(colorUniformLocation, cornerColor);

        positions.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(modelMatrix, modelMatrices.getModel());
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }

}
