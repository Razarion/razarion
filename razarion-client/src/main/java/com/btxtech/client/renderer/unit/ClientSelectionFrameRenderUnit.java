package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.Colors;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.selection.AbstractSelectionFrameRenderUnit;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 28.09.2016.
 */
@Dependent
@ColorBufferRenderer
public class ClientSelectionFrameRenderUnit extends AbstractSelectionFrameRenderUnit {
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private WebGLUniformLocation colorUniformLocation;

    @PostConstruct
    public void postConstruct() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.rgbaVpVertexShader(), Shaders.INSTANCE.rgbaVpFragmentShader()).enableTransformation(false));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        colorUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_COLOR);
    }

    @Override
    protected void fillBuffers(List<Vertex> vertices) {
        positions.fillBuffer(vertices);
    }

    @Override
    protected void draw() {
        webGlFacade.useProgram();

        positions.activate();
        webGlFacade.uniform4f(colorUniformLocation, Colors.SELECTION_FRAME);
        webGlFacade.drawArrays(WebGLRenderingContext.LINES);
    }
}
