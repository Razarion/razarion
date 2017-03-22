package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.item.ItemMarkerModelMatrices;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.selection.AbstractSelectedMarkerRendererUnit;
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
public class ClientSelectedMarkerRendererUnit extends AbstractSelectedMarkerRendererUnit {
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private FloatShaderAttribute visibilityAttribute;

    @PostConstruct
    public void postConstruct() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.commonVisibilityVertexShader(), Shaders.INSTANCE.itemMarkerFragmentShader());
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

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());

        positions.activate();
        visibilityAttribute.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        ItemMarkerModelMatrices itemMarkerModelMatrices = (ItemMarkerModelMatrices) modelMatrices;
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, itemMarkerModelMatrices.getModel());
        webGlFacade.uniform4f(WebGlFacade.U_COLOR, itemMarkerModelMatrices.getColor());
        webGlFacade.uniform1f("uRadius", itemMarkerModelMatrices.getRadius());
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
