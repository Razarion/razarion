package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;
import elemental2.webgl.WebGLRenderingContext;

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
    @Inject
    private WebGlFacade webGlFacade;
    private Vec3Float32ArrayShaderAttribute vertices;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.depthBufferVPVertexShader(), Shaders.INSTANCE.depthBufferVPFragmentShader()).enableShadowTransformation());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    protected void fillBuffer(UiTerrainSlopeTile uiTerrainSlopeTile) {
        // TODO vertices.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainSlopeTile.getTerrainSlopeTile().getVertices()));
    }

    @Override
    protected void draw(UiTerrainSlopeTile uiTerrainSlopeTile) {
        webGlFacade.useProgram();

        vertices.activate();

        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void dispose() {
        vertices.deleteBuffer();
    }

}
