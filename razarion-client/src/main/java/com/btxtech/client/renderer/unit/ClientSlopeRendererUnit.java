package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.FloatShaderAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.gameengine.planet.terrain.slope.Mesh;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.task.slope.AbstractSlopeRendererUnit;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.01.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientSlopeRendererUnit extends AbstractSlopeRendererUnit {
    // private static Logger logger = Logger.getLogger(ClientSlopeRendererUnit.class.getName());
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute normals;
    private VertexShaderAttribute tangents;
    private FloatShaderAttribute slopeFactors;
    private FloatShaderAttribute groundSplatting;
    private WebGlUniformTexture slopeTexture;
    private WebGlUniformTexture uSlopeBm;
    private WebGlUniformTexture groundSplattingTexture;
    private WebGlUniformTexture groundTopTexture;
    private WebGlUniformTexture groundTopBm;
    private WebGlUniformTexture groundBottomTexture;
    private WebGlUniformTexture groundBottomBm;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.slopeVertexShader(), Shaders.INSTANCE.slopeFragmentShader());
        vertices = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        tangents = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_TANGENT);
        slopeFactors = webGlFacade.createFloatShaderAttribute("aSlopeFactor");
        groundSplatting = webGlFacade.createFloatShaderAttribute("aGroundSplatting");
        webGlFacade.enableReceiveShadow();
    }

    @Override
    protected void fillBuffer(Slope slope, Mesh mesh) {
        slopeTexture = webGlFacade.createWebGLTexture(slope.getSlopeSkeletonConfig().getImageId(), "uSlopeTexture", "uSlopeTextureScale", slope.getSlopeSkeletonConfig().getImageScale());
        uSlopeBm = webGlFacade.createWebGLBumpMapTexture(slope.getSlopeSkeletonConfig().getBumpImageId(), "uSlopeBm", "uSlopeBmScale", slope.getSlopeSkeletonConfig().getBumpImageScale(), null);
        groundSplattingTexture = webGlFacade.createWebGLTexture(terrainUiService.getSplatting(), "uGroundSplatting");
        groundTopTexture = webGlFacade.createWebGLTexture(terrainUiService.getTopTexture(), WebGlFacade.U_GROUND_TOP_TEXTURE);
        groundTopBm = webGlFacade.createWebGLTexture(terrainUiService.getTopBm(), WebGlFacade.U_GROUND_TOP_BM);
        groundBottomTexture = webGlFacade.createWebGLTexture(terrainUiService.getGroundTexture(), WebGlFacade.U_GROUND_BOTTOM_TEXTURE);
        groundBottomBm = webGlFacade.createWebGLTexture(terrainUiService.getGroundBm(), WebGlFacade.U_GROUND_BOTTOM_BM);

        vertices.fillBuffer(mesh.getVertices());
        normals.fillBuffer(mesh.getNorms());
        tangents.fillBuffer(mesh.getTangents());
        slopeFactors.fillFloatBuffer(mesh.getSlopeFactors());
        groundSplatting.fillFloatBuffer(mesh.getSplatting());
    }


    @Override
    protected void draw(Slope slope) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.createMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_NORM_MATRIX, camera.createNormMatrix());

        webGlFacade.setLightUniforms("Slope", slope.getSlopeSkeletonConfig().getLightConfig());
        webGlFacade.setLightUniforms("Ground", terrainUiService.getGroundSkeleton().getLightConfig());

        webGlFacade.uniform1b("slopeOriented", slope.getSlopeSkeletonConfig().getSlopeOriented());

        // Slope
        webGlFacade.uniform1f("uSlopeBmDepth", slope.getSlopeSkeletonConfig().getBumpMapDepth());
        //Ground
        webGlFacade.uniform1i("uGroundTopTextureSize", (int)(terrainUiService.getTopTexture().getQuadraticEdge() * ClientGroundRendererUnit.UGLY_SIZE_FACTOR)); // TODO replace with configurable scale
        webGlFacade.uniform1i("uGroundTopBmSize", (int)(terrainUiService.getTopBm().getQuadraticEdge() * ClientGroundRendererUnit.UGLY_SIZE_FACTOR)); // TODO replace with configurable scale
        webGlFacade.uniform1f("uGroundTopBmDepth", terrainUiService.getGroundSkeleton().getTopBmDepth());
        webGlFacade.uniform1i("uGroundBottomTextureSize", (int)(terrainUiService.getGroundTexture().getQuadraticEdge() * ClientGroundRendererUnit.UGLY_SIZE_FACTOR)); // TODO replace with configurable scale
        webGlFacade.uniform1i("uGroundBottomBmSize", (int)(terrainUiService.getGroundBm().getQuadraticEdge() * ClientGroundRendererUnit.UGLY_SIZE_FACTOR)); // TODO replace with configurable scale
        webGlFacade.uniform1f("uGroundBottomBmDepth", terrainUiService.getGroundSkeleton().getBottomBmDepth());
        webGlFacade.uniform1i("uGroundSplattingSize", (int)(terrainUiService.getSplatting().getQuadraticEdge() * ClientGroundRendererUnit.UGLY_SIZE_FACTOR)); // TODO replace with configurable scale
        // Water
        webGlFacade.uniform1b("uHasWater", slope.hasWater());
        webGlFacade.uniform1f("uWaterLevel", slope.getWaterLevel());
        webGlFacade.uniform1f("uWaterGround", visualUiService.getVisualConfig().getWaterGroundLevel());

        vertices.activate();
        normals.activate();
        tangents.activate();
        slopeFactors.activate();
        groundSplatting.activate();

        slopeTexture.activate();
        uSlopeBm.activate();
        groundSplattingTexture.activate();
        groundTopTexture.activate();
        groundBottomTexture.activate();
        groundBottomBm.activate();
        groundTopBm.activate();

        webGlFacade.activateReceiveShadow();

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public void setupImages() {

    }
}
