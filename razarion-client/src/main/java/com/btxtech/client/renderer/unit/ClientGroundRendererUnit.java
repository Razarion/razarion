package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
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
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    private Vec3Float32ArrayShaderAttribute vertices;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec3Float32ArrayShaderAttribute tangents;
    private Float32ArrayShaderAttribute splattings;
    private WebGlUniformTexture topTexture;
    private WebGlUniformTexture splattingTexture;
    private WebGlUniformTexture bottomTexture;
    private WebGlUniformTexture bottomBm;
    private LightUniforms lightUniforms;
    private SpecularUniforms specularUniforms;
    private WebGLUniformLocation uBottomBmDepth;
    private WebGLUniformLocation uSplattingFadeThreshold;
    private WebGLUniformLocation uSplattingOffset;
    private WebGLUniformLocation uSplattingGroundBmMultiplicator;
    private WebGlUniformTexture terrainMarkerTexture;
    private WebGLUniformLocation terrainMarker2DPoints;
    private WebGLUniformLocation terrainMarkerAnimation;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader()).enableTransformation(true).enableReceiveShadow());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        splattings = webGlFacade.createFloat32ArrayShaderAttribute(WebGlFacade.A_GROUND_SPLATTING);
        lightUniforms = new LightUniforms(null, webGlFacade);
        specularUniforms = new SpecularUniforms(null, webGlFacade);
        uBottomBmDepth = webGlFacade.getUniformLocation("uBottomBmDepth");
        uSplattingFadeThreshold = webGlFacade.getUniformLocation("uSplattingFadeThreshold");
        uSplattingOffset = webGlFacade.getUniformLocation("uSplattingOffset");
        uSplattingGroundBmMultiplicator = webGlFacade.getUniformLocation("uSplattingGroundBmMultiplicator");
        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillBuffersInternal(UiTerrainTile uiTerrainTile) {
        topTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getTopTextureId(), "uTopTexture", "uTopTextureScale", uiTerrainTile.getTopTextureScale());
        splattingTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getSplattingId(), "uSplatting", "uSplattingScale", uiTerrainTile.getSplattingScale());
        bottomTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getBottomTextureId(), "uBottomTexture", "uBottomTextureScale", uiTerrainTile.getBottomTextureScale());
        bottomBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainTile.getBottomBmId(), "uBottomBm", "uBottomBmScale", uiTerrainTile.getBottomBmScale(), "uBottomBmOnePixel");

        vertices.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundPositions()));
        normals.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundNorms()));
        // TODO tangents.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundTangents()));
        // TODO splattings.fillFloat32Array(WebGlUtil.doublesToFloat32Array(uiTerrainTile.getTerrainTile().getGroundSplattings()));
    }

    @Override
    public void draw(UiTerrainTile uiTerrainTile) {
        webGlFacade.useProgram();

        lightUniforms.setLightUniforms(webGlFacade);
        specularUniforms.setUniforms(uiTerrainTile.getSpecularLightConfig(), webGlFacade);
        webGlFacade.uniform1f(uBottomBmDepth, uiTerrainTile.getBottomBmDepth());
        // TODO  webGlFacade.uniform1f(uSplattingFadeThreshold, uiTerrainTile.getSplattingFadeThreshold());
        // TODO  webGlFacade.uniform1f(uSplattingOffset, uiTerrainTile.getSplattingOffset());
        webGlFacade.uniform1f(uSplattingGroundBmMultiplicator, uiTerrainTile.getSplattingGroundBmMultiplicator());

        webGlFacade.activateReceiveShadow();

        vertices.activate();
        normals.activate();
        tangents.activate();
        splattings.activate();

        topTexture.overrideScale(uiTerrainTile.getTopTextureScale());
        topTexture.activate();
        splattingTexture.overrideScale(uiTerrainTile.getSplattingScale());
        splattingTexture.activate();
        bottomTexture.overrideScale(uiTerrainTile.getBottomTextureScale());
        bottomTexture.activate();
        bottomBm.overrideScale(uiTerrainTile.getBottomBmScale());
        bottomBm.activate();
        if (inGameQuestVisualizationService.isQuestInGamePlaceVisualization()) {
            terrainMarkerTexture.activate();
            webGlFacade.uniform4f(terrainMarker2DPoints, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfigBoundary());
            webGlFacade.uniform1f(terrainMarkerAnimation, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getAnimation());
        } else {
            webGlFacade.uniform4f(terrainMarker2DPoints, 0, 0, 0, 0);
        }
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
