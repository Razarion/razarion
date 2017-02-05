package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.task.particle.AbstractParticleRenderUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 03.02.2017.
 */
@DepthBufferRenderer
@Dependent
public class ClientParticleDepthBufferRenderUnit extends AbstractParticleRenderUnit {
    // private Logger logger = Logger.getLogger(ClientParticleDepthBufferRenderUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private Camera camera;
    private VertexShaderAttribute positions;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.rgbaMvpVertexShader(), Shaders.INSTANCE.particleDeptBufferFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    protected void fillBuffers(List<Vertex> vertices, List<DecimalPosition> alphaTextureCoordinates) {
        positions.fillBuffer(vertices);
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, shadowUiService.getDepthViewTransformation());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, shadowUiService.getDepthProjectionTransformation());

        positions.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
