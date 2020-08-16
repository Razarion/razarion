package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.WebGlGroundMaterial;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.questvisualization.InGameQuestVisualizationService;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.task.ground.AbstractGroundRendererUnit;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

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
    private GameUiControl gameUiControl;
    @Inject
    private InGameQuestVisualizationService inGameQuestVisualizationService;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute normals;
    private WebGlGroundMaterial webGlGroundMaterial;
    private LightUniforms lightUniforms;
//    private WebGlUniformTexture terrainMarkerTexture;
//    private WebGLUniformLocation terrainMarker2DPoints;
//    private WebGLUniformLocation terrainMarkerAnimation;

    @Override
    public void init() {
        webGlFacade.enableOESStandartDerivatives();
        webGlFacade.init(new WebGlFacadeConfig(this, Shaders.INSTANCE.groundVertexShader(), Shaders.INSTANCE.groundFragmentShader()).enableTransformation(true).enableReceiveShadow().enableCastShadow());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        lightUniforms = new LightUniforms(webGlFacade);
        webGlGroundMaterial = new WebGlGroundMaterial(webGlFacade, gameUiControl);
//        terrainMarkerTexture = webGlFacade.createTerrainMarkerWebGLTexture("uTerrainMarkerTexture");
//        terrainMarker2DPoints = webGlFacade.getUniformLocation("uTerrainMarker2DPoints");
//        terrainMarkerAnimation = webGlFacade.getUniformLocation("uTerrainMarkerAnimation");
    }

    @Override
    protected void fillBuffersInternal(UiTerrainGroundTile uiTerrainGroundTile) {
        webGlGroundMaterial.init(uiTerrainGroundTile.getGroundConfig());

        Float32Array groundPositions = Js.uncheckedCast(uiTerrainGroundTile.getGroundPositions());
        positions.fillFloat32Array(groundPositions);
        normals.fillFloat32Array(Js.uncheckedCast(uiTerrainGroundTile.getGroundNorms()));
        setElementCount((int) (groundPositions.length / Vertex.getComponentsPerVertex()));
    }

    @Override
    public void draw(UiTerrainGroundTile uiTerrainGroundTile) {
        webGlFacade.useProgram();
        webGlFacade.setTransformationUniforms();

        lightUniforms.setLightUniforms(webGlFacade);

        webGlFacade.activateReceiveShadow();

        positions.activate();
        normals.activate();

        webGlGroundMaterial.activate();

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
        positions.deleteBuffer();
        normals.deleteBuffer();
    }

    @Override
    public List<String> getGlslFragmentDefines() {
        if (getRenderData().getGroundConfig().getBottomMaterial() != null && getRenderData().getGroundConfig().getSplatting() != null) {
            return Collections.singletonList("RENDER_GROUND_BOTTOM_TEXTURE");
        }
        return null;
    }
}
