package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.selection.AbstractStatusBarRendererUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2017.
 */
@Dependent
@ColorBufferRenderer
public class ClientStatusBarRendererUnit extends AbstractStatusBarRendererUnit {
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private FloatShaderAttribute visibilityAttribute;

    @PostConstruct
    public void postConstruct() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.commonVisibilityVertexShader(), Shaders.INSTANCE.statusBarFragmentShader()).enableTransformation(false));
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        visibilityAttribute = webGlFacade.createFloatShaderAttribute("aVisibility");
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
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.uniform4f(WebGlFacade.U_COLOR, modelMatrices.getColor());
        webGlFacade.uniform4f("uBgColor", modelMatrices.getBgColor());
        webGlFacade.uniform1f("uProgress", modelMatrices.getProgress());
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
