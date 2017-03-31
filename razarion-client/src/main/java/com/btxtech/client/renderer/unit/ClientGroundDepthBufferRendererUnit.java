package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainTile;
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
public class ClientGroundDepthBufferRendererUnit extends AbstractGroundRendererUnit {
    // private Logger logger = Logger.getLogger(ClientGroundDepthBufferRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    private Vec3Float32ArrayShaderAttribute vertices;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.depthBufferVPVertexShader(), Shaders.INSTANCE.depthBufferVPFragmentShader()).enableShadowTransformation());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    protected void fillBuffersInternal(UiTerrainTile uiTerrainTile) {
        // vertices.fillFloat32ArrayEmu(uiTerrainTile.getVertices());
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void draw(UiTerrainTile uiTerrainTile) {
        webGlFacade.useProgram();

        vertices.activate();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
