package com.btxtech.client.editor.widgets.marker;

import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 11.09.2015.
 */
@ColorBufferRenderer
@Dependent
public class TerrainMarkerEditorRendererUnit extends AbstractRenderUnit<List<Vertex>> {
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private WebGLUniformLocation colorUniformLocation;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(Shaders.SHADERS.rgbaVpVertexShader(), Shaders.SHADERS.rgbaFragmentShader()));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        colorUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_COLOR);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(List<Vertex> triangles) {
        positions.fillBuffer(triangles);
        setElementCount(triangles);
    }

    @Override
    protected void prepareDraw() {

    }

    @Override
    public void draw(ModelMatrices modelMatrices) {
        webGlFacade.useProgram();

        webGlFacade.uniform4f(colorUniformLocation, new Color(0, 1, 0, 0.5));

        positions.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
