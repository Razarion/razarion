package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainTile;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.05.2015.
 */
@ColorBufferRenderer
@Dependent
public class ClientGroundRendererUnit extends AbstractGroundRendererUnit {
    // private Logger logger = Logger.getLogger(ClientGroundRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    private Vec3Float32ArrayShaderAttribute vertices;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec3Float32ArrayShaderAttribute tangents;
    private Float32ArrayShaderAttribute splattings;
    private WebGlUniformTexture topTexture;
    private WebGlUniformTexture topBm;
    private WebGlUniformTexture splattingTexture;
    private WebGlUniformTexture bottomTexture;
    private WebGlUniformTexture bottomBm;
    private LightUniforms lightUniforms;
    private WebGLUniformLocation uTopBmDepth;
    private WebGLUniformLocation uBottomBmDepth;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader()).enableTransformation(true).enableReceiveShadow());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        splattings = webGlFacade.createFloat32ArrayShaderAttribute(WebGlFacade.A_GROUND_SPLATTING);
        lightUniforms = new LightUniforms(null, webGlFacade);
        uTopBmDepth = webGlFacade.getUniformLocation("uTopBmDepth");
        uBottomBmDepth = webGlFacade.getUniformLocation("uBottomBmDepth");
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillBuffersInternal(UiTerrainTile uiTerrainTile) {
        topTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getTopTextureId(), "uTopTexture", "uTopTextureScale", uiTerrainTile.getTopTextureScale());
        topBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainTile.getTopBmId(), "uTopBm", "uTopBmScale", uiTerrainTile.getTopBmScale(), "uTopBmOnePixel");
        splattingTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getSplattingId(), "uSplatting", "uSplattingScale", uiTerrainTile.getSplattingScale());
        bottomTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getBottomTextureId(), "uBottomTexture", "uBottomTextureScale", uiTerrainTile.getBottomTextureScale());
        bottomBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainTile.getBottomBmId(), "uBottomBm", "uBottomBmScale", uiTerrainTile.getBottomBmScale(), "uBottomBmOnePixel");

        vertices.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundVertices()));
        normals.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundNorms()));
        tangents.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundTangents()));
        splattings.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundSplattings()));
    }

    @Override
    public void draw(UiTerrainTile uiTerrainTile) {
        webGlFacade.useProgram();

        lightUniforms.setLightUniforms(uiTerrainTile.getGroundLightConfig(), webGlFacade);
        webGlFacade.uniform1f(uTopBmDepth, uiTerrainTile.getTopBmDepth());
        webGlFacade.uniform1f(uBottomBmDepth, uiTerrainTile.getBottomBmDepth());

        webGlFacade.activateReceiveShadow();

        vertices.activate();
        normals.activate();
        tangents.activate();
        splattings.activate();

        topTexture.overrideScale(uiTerrainTile.getTopTextureScale());
        topTexture.activate();
        topBm.overrideScale(uiTerrainTile.getTopBmScale());
        topBm.activate();
        splattingTexture.overrideScale(uiTerrainTile.getSplattingScale());
        splattingTexture.activate();
        bottomTexture.overrideScale(uiTerrainTile.getBottomTextureScale());
        bottomTexture.activate();
        bottomBm.overrideScale(uiTerrainTile.getBottomBmScale());
        bottomBm.activate();

        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void dispose() {
        vertices.deleteBuffer();
        normals.deleteBuffer();
        tangents.deleteBuffer();
        splattings.deleteBuffer();
    }
}
