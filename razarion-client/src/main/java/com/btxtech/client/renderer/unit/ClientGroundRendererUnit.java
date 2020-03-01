package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainTile;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import jsinterop.base.Js;

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
//    private WebGlUniformTexture topTexture;
//    private WebGlUniformTexture splattingTexture;
//    private WebGlUniformTexture bottomTexture;
//    private WebGlUniformTexture bottomBm;
//    private LightUniforms lightUniforms;
//    private SpecularUniforms specularUniforms;
//    private WebGLUniformLocation uBottomBmDepth;
//    private WebGLUniformLocation uSplattingGroundBmMultiplicator;
//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

    @PostConstruct
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader()).enableTransformation(true).enableReceiveShadow());
        vertices = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
//        lightUniforms = new LightUniforms(null, webGlFacade);
//        specularUniforms = new SpecularUniforms(null, webGlFacade);
//        uBottomBmDepth = webGlFacade.getUniformLocation("uBottomBmDepth");
//        uSplattingGroundBmMultiplicator = webGlFacade.getUniformLocation("uSplattingGroundBmMultiplicator");
//        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
//        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
//        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void fillBuffersInternal(UiTerrainTile uiTerrainTile) {
//        topTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getTopTextureId(), "uTopTexture", "uTopTextureScale", uiTerrainTile.getTopTextureScale());
//        splattingTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getSplattingId(), "uSplatting", "uSplattingScale", uiTerrainTile.getSplattingScale());
//        bottomTexture = webGlFacade.createWebGLTexture(uiTerrainTile.getBottomTextureId(), "uBottomTexture", "uBottomTextureScale", uiTerrainTile.getBottomTextureScale());
//        bottomBm = webGlFacade.createWebGLBumpMapTexture(uiTerrainTile.getBottomBmId(), "uBottomBm", "uBottomBmScale", uiTerrainTile.getBottomBmScale(), "uBottomBmOnePixel");

        Float32Array groundPositions = Js.uncheckedCast(uiTerrainTile.getTerrainTile().getGroundPositions());
        vertices.fillFloat32Array(groundPositions);
        normals.fillFloat32Array(Js.uncheckedCast(uiTerrainTile.getTerrainTile().getGroundNorms()));
        setElementCount((int) (groundPositions.length / Vertex.getComponentsPerVertex()));
    }

    @Override
    public void draw(UiTerrainTile uiTerrainTile) {
        webGlFacade.useProgram();

//        lightUniforms.setLightUniforms(webGlFacade);
//        specularUniforms.setUniforms(uiTerrainTile.getSpecularLightConfig(), webGlFacade);
//        webGlFacade.uniform1f(uBottomBmDepth, uiTerrainTile.getBottomBmDepth());
//        webGlFacade.uniform1f(uSplattingGroundBmMultiplicator, uiTerrainTile.getSplattingGroundBmMultiplicator());

        webGlFacade.activateReceiveShadow();

        vertices.activate();
        normals.activate();

//        topTexture.overrideScale(uiTerrainTile.getTopTextureScale());
//        topTexture.activate();
//        splattingTexture.overrideScale(uiTerrainTile.getSplattingScale());
//        splattingTexture.activate();
//        bottomTexture.overrideScale(uiTerrainTile.getBottomTextureScale());
//        bottomTexture.activate();
//        bottomBm.overrideScale(uiTerrainTile.getBottomBmScale());
//        bottomBm.activate();
//        if (inGameQuestVisualizationService.isQuestInGamePlaceVisualization()) {
//            terrainMarkerTexture.activate();
//            webGlFacade.uniform4f(terrainMarker2DPoints, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getPlaceConfigBoundary());
//            webGlFacade.uniform1f(terrainMarkerAnimation, inGameQuestVisualizationService.getQuestInGamePlaceVisualization().getAnimation());
//        } else {
//            webGlFacade.uniform4f(terrainMarker2DPoints, 0, 0, 0, 0);
//        }
        // Draw
        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void dispose() {
        vertices.deleteBuffer();
        normals.deleteBuffer();
    }
}
