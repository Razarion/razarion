package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@DepthBufferRenderer
@Dependent
public class ClientSlopeDepthBufferRendererUnit extends AbstractSlopeRendererUnit {
    // private Logger logger = Logger.getLogger(ClientSlopeDepthBufferRendererUnit.class.getName());
    private Vec3Float32ArrayShaderAttribute vertices;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ShadowUiService shadowUiService;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.depthBufferVPVertexShader(), Shaders.INSTANCE.depthBufferVPFragmentShader());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    protected void fillBuffer(SlopeUi slopeUi) {
        vertices.fillFloat32ArrayEmu(slopeUi.getVertices());
    }

    @Override
    protected void draw(SlopeUi slopeUi) {
        webGlFacade.useProgram();
        // Projection uniform
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, shadowUiService.getDepthProjectionTransformation());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, shadowUiService.getDepthViewTransformation());

        vertices.activate();

        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
