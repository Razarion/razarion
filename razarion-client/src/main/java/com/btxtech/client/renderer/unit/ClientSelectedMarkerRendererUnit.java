package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.selection.AbstractSelectedMarkerRendererUnit;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2017.
 */
@Dependent
@ColorBufferRenderer
public class ClientSelectedMarkerRendererUnit extends AbstractSelectedMarkerRendererUnit {
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private FloatShaderAttribute visibilityAttribute;
    private WebGLUniformLocation colorUniformLocation;
    private WebGLUniformLocation modelMatrix;
    private WebGLUniformLocation uRadius;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.commonVisibilityVertexShader(), Shaders.INSTANCE.itemMarkerFragmentShader()).enableTransformation(false));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        visibilityAttribute = webGlFacade.createFloatShaderAttribute("aVisibility");
        colorUniformLocation = webGlFacade.getUniformLocation(WebGlFacade.U_COLOR);
        modelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_MATRIX);
        uRadius = webGlFacade.getUniformLocation("uRadius");
    }

    @Override
    protected void fillBuffers(List<Vertex> vertices, List<Double> visibilities) {
        positions.fillBuffer(vertices);
        visibilityAttribute.fillDoubleBuffer(visibilities);
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        positions.activate();
        visibilityAttribute.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(modelMatrix, modelMatrices.getModel());
        webGlFacade.uniform4f(colorUniformLocation, modelMatrices.getColor());
        webGlFacade.uniform1f(uRadius, modelMatrices.getRadius());
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
