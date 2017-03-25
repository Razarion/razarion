package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.tip.AbstractInGameTipCornerRendererUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 07.12.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientInGameTipCornerRendererUnit extends AbstractInGameTipCornerRendererUnit {
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;

    @PostConstruct
    public void postConstruct() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.rgbaMvpVertexShader(), Shaders.INSTANCE.rgbaVpFragmentShader()).enableTransformation(false));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    protected void fillBuffers(List<Vertex> vertices) {
        positions.fillBuffer(vertices);
    }

    @Override
    protected void prepareDraw(Color cornerColor) {
        webGlFacade.useProgram();

        webGlFacade.uniform4f(WebGlFacade.U_COLOR, cornerColor);

        positions.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }

}
