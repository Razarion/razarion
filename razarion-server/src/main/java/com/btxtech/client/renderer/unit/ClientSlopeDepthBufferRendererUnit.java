package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractSlopeRendererUnit;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;
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
    private VertexShaderAttribute vertices;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ShadowUiService shadowUiService;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.depthBufferVPVertexShader(), Shaders.INSTANCE.depthBufferVPFragmentShader());
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    protected void fillBuffers(Slope slope) {
        vertices.fillBuffer(slope.getMesh().getVertices());
        setElementCount(slope.getMesh());
    }

    @Override
    public void draw(Slope slope) {
        webGlFacade.useProgram();
        // Projection uniform
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, shadowUiService.createDepthProjectionTransformation());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, shadowUiService.createDepthViewTransformation());

        vertices.activate();

        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
